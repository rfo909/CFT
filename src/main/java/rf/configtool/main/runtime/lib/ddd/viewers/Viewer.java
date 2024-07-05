/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main.runtime.lib.ddd.viewers;

import java.io.File;

import rf.configtool.main.runtime.lib.ddd.core.*;

/**
* This is a common superclass for Viewer types. A viewer is someone able to
* process triangles. This class also implements a few very important common
* methods, among them the constructor that takes care of ordinary camera-type
* parameters, but also creates the 3d-planes that define the view-cone, as
* used in the notVisible() method that performs 3d-clipping against the
* viewcone, so that objects outside of it can abort generation of sub-objects
* and triangles. The notVisible() method is implemented in this class. It may
* be redefined in subclasses, but should probably still be called via the super-
* class.
*/
public abstract class Viewer implements TriangleReceiver {
    
    protected static Vector3d origoVector=new Vector3d(0,0,0);
    
    protected double focalLength;
    protected double filmWidth;
    protected double filmHeight;
    protected int sizex, sizey;
    
    private Plane3d[] viewConePlanes=new Plane3d[0];
        // Private: not accessible for subclasses. Set in constructor. Given
        // default value in case subclasses forget to call the constructor in
        // this class. 
        // Planes should be defined with "outside" being outside the viewcone.
    
    /**
    * This constructor saves the given parameters in protected class variables.
    * In addition, it generates the necessary Plane3d objects to implement the
    * cancelObject() method. All subclass constructors should start with a
    * call to this one.
    */
    
    protected Viewer (double focalLength,   
        double filmWidth, double filmHeight, int sizex, int sizey) 
    {
        this.focalLength=focalLength;
        this.filmWidth=filmWidth;
        this.filmHeight=filmHeight;
        this.sizex=sizex;
        this.sizey=sizey;
        
        Vector3d upperLeft=new Vector3d(focalLength, filmWidth/2, filmHeight/2);
        Vector3d upperRight=new Vector3d(focalLength, -filmWidth/2, filmHeight/2);
        Vector3d lowerRight=new Vector3d(focalLength, -filmWidth/2, -filmHeight/2);
        Vector3d lowerLeft=new Vector3d(focalLength, filmWidth/2, -filmHeight/2);
        
        viewConePlanes=new Plane3d[4];
        viewConePlanes[0]=new Plane3d(origoVector, lowerRight, upperRight);
        viewConePlanes[1]=new Plane3d(origoVector, upperRight, upperLeft);
        viewConePlanes[2]=new Plane3d(origoVector, upperLeft, lowerLeft);
        viewConePlanes[3]=new Plane3d(origoVector, lowerLeft, lowerRight);
    }
    
    /**
    * Clear the viewer so that it can start producing a fresh image.
    */
    public abstract void clear();

    /**
    * Draw visible triangles only. The corners should be given in anti-clockwise order
    * as seen from the outside, since this method solves the plane equation 
    * to determine whether the eye is on the "outside" or not. 
    */
    public abstract void triVisible (Triangle tri);

    /**
    * Draw a triangle without calculating whether or not the eye is on the
    * outside of the triangle. Must use this method for all but closed shapes or
    * one has to be very careful.
    */
    public abstract void tri (Triangle tri);

    /**
    * Calculate the pixel position that the given vector from origo
    * passes through. If the given point is behind the eye (x=0) then
    * returns null.
    */
    protected ScreenCoordinates calcScreenCoordinates (Vector3d p) {
        // if (p.getX() <= focalLength) return null;        // behind film plane : reject
        Vector3d q=p.mul(focalLength / p.getX());  // vector that points from eye to film
        
        double pix_x=(sizex/2)-(q.getY()* sizex/filmWidth);
        double pix_y=(sizey/2)-(q.getZ()* sizey/filmHeight);
        
        ScreenCoordinates sc=new ScreenCoordinates(pix_x, pix_y);
        return sc;
    }
    
    protected ScreenCoordinates[] calcScreenCoordinates (Vector3d points[]) {
        ScreenCoordinates arr[]=new ScreenCoordinates[points.length];
        for (int i=0; i<points.length; i++) {
            arr[i]=calcScreenCoordinates(points[i]);
        }
        return arr;
    }

    /**
    * This method is the opposite of calcScreenCoordinates, in that it takes as
    * argument a set of coordinates and returns the 3d-vector that passes through
    * the center of the pixel as represented. 
    */
    protected Vector3d calcScreenVector (double pix_x, double pix_y) {
        double real_y = ((sizex/2) - pix_x) / (sizex / filmWidth);
        double real_z = ((sizey/2) - pix_y) / (sizey / filmHeight);
        return new Vector3d(focalLength, real_y, real_z);
    }
    
    /**
     * Write PNG file
     */
    public abstract void writePNG (File file) throws Exception;
    
    
    /**
    * This method returns true if the object whose bounds are given as parameter
    * will definitely not be shown in the viewer. This base implementation performs
    * checks against the viewcone of this viewer, in that if the bounds are completely
    * outside the viewcone, the method returns true. Additional functionality may be added
    * by overriding the method, making sure to call the superclass version of the method
    * first. The method name is the way it is because non-visibility is the only thing one
    * can know by looking at bounds only. 
    * <p>
    * Objects at all levels can calculate their own bounds (if known) and call this
    * method in their viewer, to effectively prune the world tree of systems and subsystems.
    * <b>Note</b> however, that triangles should not be checked, as these are the
    * responsibility of the viewer.
    */
    public boolean notVisible (Bounds3d bounds) {
        for (int i=0; i<viewConePlanes.length; i++) {
            if (!(bounds.inside(viewConePlanes[i]))) {
                return true;
            }
        }
        return false;
    }
    
    /**
    * This protected method should be used by the triange rendering methods:
    * if it returns true, then abort, since no parts of the triangle can
    * possibly be visible.
    */
    protected boolean triangleNotVisible (Triangle t) {
        Bounds3d b=new Bounds3d(t.getPoints());
        return notVisible(b);
    }
        
}
