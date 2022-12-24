/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

package rf.configtool.main.runtime.lib.ddd.core;

import java.awt.Color;
import java.util.*;


/**
* A brush is a set of 3d-vectors. Applying these in a state (Ref) produces a set of new
* Ref instances. Repeating the process with other states means dragging a list of
* connected points through space - it describes a surface. 
*/

public class Brush {
    
    private TriangleReceiver triDest;
    private VisibleAttributes attr;
    
    private Vector points=new Vector();         // Vector3d
    private Vector prevPoints=null;
    private Ref prevRef=null;
    
    private List<Triangle> terminatorTriangles=new ArrayList<Triangle>();
    
    private boolean visibleTrianglesOnly=false;
    // decides whether calling triVisible() or tri() in triDest.
    private boolean splitBothWays=true;
    // if true, generate double set of triangles, splitting both possible ways
    
    
    
    /**
    * Create a new brush, giving as argument some object capable of further processing
    * of the triangles that will be generated when the brush is used.
    * Remember also to call setAttributes() before creating segments,
    * or they will be all red (default).
    */
    public Brush (TriangleReceiver triDest) {
        this.triDest=triDest;
        this.attr=new VisibleAttributes(Color.BLACK);
    }
    
    public void setAttr (VisibleAttributes attr) {
        this.attr=attr;
    }
    
    /**
    * Private helper method to add a point and an accompanying attributes object
    * to the two vectors that are stored internally. 
    */
    private void add (Vector3d point) {
        points.addElement(point);
    }
    
    /**
    * Create the first or a new starting point for a sequence of segments.
    */
    public void addPoint (Vector3d point) {
        add(point);
    }
    
    public void addPoint (double x, double y, double z) {
        addPoint(new Vector3d(x,y,z));
    }
    
    public void addTerminatorTriangle (Triangle t) {
        terminatorTriangles.add(t);
    }

    /** Call this method to change what triangles are displayed. If b is true, then
    * only triangles for which the eye is on the outside, are visible. If b is false,
    * the all triangles will be displayed. Default is TRUE !!!
    */
    public void setVisibleTrianglesOnly (boolean b) {
        this.visibleTrianglesOnly=b;
    }
    
    /** Call this method to change how triangles are generated when the brush is moved.
    * Defaults to false, creating two triangles for each rectangle. If true, both ways
    * of dividing the triangle will be used. This may be suitable when the brush twists
    * a lot from one penDown() to the next. Naturally this will make the triDest work
    * twice as hard as well...
    */
    public void setSplitBothWays (boolean b) {
        this.splitBothWays=b;
    }
    
    /**
    * This method lifts the virtual pen of the brush, making it possible to do
    * a penDown somewhere else without generating the in-between "line".
    */
    public void penUp() {
        if (prevRef != null) {
            terminator(prevRef);
            prevRef=null;
        }
        prevPoints=null;
    }
    
    /**
     * Map terminator triangles to ref position
     */
    private void terminator (Ref ref) {
        for (Triangle t:terminatorTriangles) {
            Vector3d[] points = t.getPoints();
            Vector3d a=ref.translate(points[0]).getPos();
            Vector3d b=ref.translate(points[1]).getPos();
            Vector3d c=ref.translate(points[2]).getPos();
            triDest.tri(new Triangle(a,b,c,t.getAttributes()));
        }
    }
    
    /**
    * Starts a new "line" or contines drawing from the previous penDown() position,
    * unless penUp() has been called inbetween.
    */
    public void penDown(Ref ref) {
        if (prevRef==null) {
            terminator(ref);
        }
        Vector newPoints=new Vector();
        for (int i=0; i<points.size(); i++) {
            Vector3d transVector=(Vector3d) points.elementAt(i);
            newPoints.addElement(ref.translate(transVector).getPos());
        }
        if (prevPoints != null) {
            // start at 1 since the first must be a point without attributes
            for (int i=1; i<prevPoints.size(); i++) {
                Vector3d prev_a=(Vector3d) prevPoints.elementAt(i-1);
                Vector3d prev_b=(Vector3d) prevPoints.elementAt(i);
                Vector3d new_a=(Vector3d) newPoints.elementAt(i-1);
                Vector3d new_b=(Vector3d) newPoints.elementAt(i);
                generateTriangles (prev_a, prev_b, new_a, new_b, attr);
            }
        }
        prevPoints=newPoints;
        prevRef=ref;
    }
    
    
    /**
    * Helper method to generate triangles from four points. Optimized so that if
    * visibleTrianglesOnly and the first triangle is found to be not visible, then
    * it aborts without generating triangles. If that passes, then the first two 
    * triangles are sent to the triDest. Then, if splitBothWays is set, another pair
    * of triangles are given to the triDest, splitted along the other diagonal.
    */
    private void generateTriangles (Vector3d prev_a, Vector3d prev_b, Vector3d new_a, Vector3d new_b,
        VisibleAttributes attr) 
    {
        Triangle t=new Triangle (prev_a, prev_b, new_b, attr);
        if (visibleTrianglesOnly) {
            if (t.calcPlaneEquation(0.0, 0.0, 0.0) <= 0) {
                // eye is in or behind the plane
                return;
            }
        }
        triDest.tri(t);
        triDest.tri(new Triangle(prev_a, new_b, new_a, attr));
        if (splitBothWays) {
            triDest.tri(new Triangle(prev_a, prev_b, new_a, attr));
            triDest.tri(new Triangle(new_a, prev_b, new_b, attr));
        }
    }
    
}
