/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

/**
*
* This class represents the most basic primitive for "addressing" the 3d space. It
* is a local reference system that has a position vector (relative to whatever),
* an orientation expressed by three unit vectors, and a scale, as expressed by the length
* of its unit vectors.
*
* By its position it represents a point in space, and by its other attributes it also
* represents a means for generating other relevant points.
*
* All instances are always immutable. That means that all operations such as
* rotation, translation and scaling, return new instances. This is a very important
* feature, as it means one can pass a Ref object anywhere and trust it will never
* change.
*
*
* History:
*
* December 4 and 5, 1999: first version
*/

public class Ref {

    protected static final Vector3d unit_x=new Vector3d(1,0,0);
    protected static final Vector3d unit_y=new Vector3d(0,1,0);
    protected static final Vector3d unit_z=new Vector3d(0,0,1);

    private Vector3d pos;  // pos of origo expressed in global coordinates
    private Vector3d x;  // unit vectors, expressed in global coordinates
    private Vector3d y;
    private Vector3d z;


    /**
    * Default constructor: creates ref that is positioned at (0,0,0) and unit vectors
    * of length one.
    */
    public Ref () {
        pos=new Vector3d(0,0,0);
        x=unit_x;
        y=unit_y;
        z=unit_z;
    }

    /**
    * Create object from parts. Mostly used internally in this class.
    */
    public Ref (Vector3d pos, Vector3d x, Vector3d y, Vector3d z) {
        this.pos=pos;
        this.x=x;
        this.y=y;
        this.z=z;
    }

    // ------------------------------------------------------------------------------
    // Get-methods
    // ------------------------------------------------------------------------------

    /** Returns position vector of Ref object - may be global or relative to other Ref system */
    public Vector3d getPos() {
        return pos;  // may return the original since Vector3d objects are immutable
    }
    
    public Ref setPos (Vector3d pos) {
    	return new Ref(pos, x, y, z);
    }

    public Vector3d getUnitVectorX() {
        return x;
    }

    public Vector3d getUnitVectorY() {
        return y;
    }

    public Vector3d getUnitVectorZ() {
        return z;
    }
    // --------------------------------------------------------------
    // Core operations: rotation around local axis
    // --------------------------------------------------------------

    /** Creates new Ref rotated relative to x-axis */
    public Ref rotateX (double radians) {
        Vector3d ny=unit_y.rotateX(radians).transform(x,y,z);
        Vector3d nz=unit_z.rotateX(radians).transform(z,y,z);
        return new Ref (pos, x, ny, nz);
    }

    /** Creates new Ref rotated around y-axis */
    public Ref rotateY (double radians) {
        Vector3d nx=unit_x.rotateY(radians).transform(x,y,z);
        Vector3d nz=unit_z.rotateY(radians).transform(x,y,z);
        return new Ref (pos, nx, y, nz);
    }

    /** Creates new Ref rotated around z-axis */
    public Ref rotateZ (double radians) {
        Vector3d nx=unit_x.rotateZ(radians).transform(x,y,z);
        Vector3d ny=unit_y.rotateZ(radians).transform(x,y,z);
        return new Ref (pos, nx, ny, z);
    }

    // --------------------------------------------------------------
    // Core operations: translate along local unit-vectors
    // --------------------------------------------------------------

    /** Creates new Ref with relative position as given by parameters */
    public Ref translate(double distx, double disty, double distz) {
        Vector3d newPos=pos.add(x.mul(distx)).add(y.mul(disty)).add(z.mul(distz));
        return new Ref(newPos, x, y, z);
    }

    // --------------------------------------------------------------
    // Core operations: scaling unit vectors
    // --------------------------------------------------------------

    /**
    * Creates new Ref with unit vectors scaled as given. <b>NOTE</b>: be careful
    * about using negative values - they should only be used in pairs, so that the
    * resulting system is still right-hand. If reversing only one of the axes, then
    * the "positive determinant means eye outside object" method will fail.
    */
    public Ref scale(double factorx, double factory, double factorz) {
        return new Ref (pos, x.mul(factorx), y.mul(factory), z.mul(factorz));
    }

    public double getScaleFactor() {
        return x.length();
    }

    public Ref setScaleFactor(double factor) {
        double currScale=getScaleFactor();
        double divFactor=factor/currScale;
        return scale(divFactor, divFactor, divFactor);
    }

    // --------------------------------------------------------------
    // Core operations: transform from local system to global system
    // --------------------------------------------------------------

    /**
    * Transform given vector to global coordinate system by
    * multiplying its components with the given unit vectors, adding the
    * result to create a new vector that is local to the system in which the
    * unit vectors are expressed.
    */
    public Vector3d transformLocalToGlobal (Vector3d localVec) {
        return x.mul(localVec.getX()).add(y.mul(localVec.getY())).add(z.mul(localVec.getZ()));
    }

    // --------------------------------------------------------------
    //
    //                         Utility methods
    //
    //    (these all use the core operations to do the actual work)
    // --------------------------------------------------------------


    /** Creates new Ref with relative position as given by vector */
    public Ref translate (Vector3d v) {
        return translate (v.getX(), v.getY(), v.getZ());
    }

    public Ref rotateDegX (double degrees) {
        return rotateX (Math.PI * degrees / 180.0);
    }

    public Ref rotateDegY (double degrees) {
        return rotateY (Math.PI * degrees / 180.0);
    }

    public Ref rotateDegZ (double degrees) {
        return rotateZ (Math.PI * degrees / 180.0);
    }

    /** Turn a fraction of the circle */
    public Ref turnLeftFraction (double fraction) {
        return rotateZ (fraction * Math.PI * 2);
    }

    /** Turn a fraction of the circle, the fraction being (step/totalSteps). Convenience method */
    public Ref turnLeftFraction (int step, int totalSteps) {
        double fraction=((double) step) / ((double) totalSteps);
        return turnLeftFraction(fraction);
    }


    /** Turn a fraction of the circle */
    public Ref turnRightFraction (double fraction) {
        return rotateZ (-(fraction * Math.PI *2));
    }

    /** Turn a fraction of the circle, the fraction being (step/totalSteps). Convenience method */
    public Ref turnRightFraction (int step, int totalSteps) {
        double fraction=((double) step) / ((double) totalSteps);
        return turnRightFraction(fraction);
    }
    /** Turn a fraction of the circle */
    public Ref rollLeftFraction (double fraction) {
        return rotateX (-(fraction * Math.PI * 2));
    }

    /** Turn a fraction of the circle, the fraction being (step/totalSteps). Convenience method */
    public Ref rollLeftFraction (int step, int totalSteps) {
        double fraction=((double) step) / ((double) totalSteps);
        return rollLeftFraction(fraction);
    }
    /** Turn a fraction of the circle */
    public Ref rollRightFraction (double fraction) {
        return rotateX (fraction * Math.PI * 2);
    }

    /** Turn a fraction of the circle, the fraction being (step/totalSteps). Convenience method */
    public Ref rollRightFraction (int step, int totalSteps) {
        double fraction=((double) step) / ((double) totalSteps);
        return rollRightFraction(fraction);
    }

    /** Turn a fraction of the circle */
    public Ref turnDownFraction (double fraction) {
        return rotateY (fraction * Math.PI * 2);
    }

    /** Turn a fraction of the circle, the fraction being (step/totalSteps). Convenience method */
    public Ref turnDownFraction (int step, int totalSteps) {
        double fraction=((double) step) / ((double) totalSteps);
        return turnDownFraction(fraction);
    }

    /** Turn a fraction of the circle */
    public Ref turnUpFraction (double fraction) {
        return rotateY (- (fraction * Math.PI * 2));
    }

    /** Turn a fraction of the circle, the fraction being (step/totalSteps). Convenience method */
    public Ref turnUpFraction (int step, int totalSteps) {
        double fraction=((double) step) / ((double) totalSteps);
        return turnUpFraction(fraction);
    }


    /** Rotate a number of degrees */
    public Ref turnLeftDeg (double degrees) {
        return rotateDegZ(degrees);
    }

    /** Rotate a number of degrees */
    public Ref turnRightDeg (double degrees) {
        return rotateDegZ(-degrees);
    }

    /** Rotate a number of degrees */
    public Ref rollLeftDeg (double degrees) {
        return rotateDegX (-degrees);
    }

    /** Rotate a number of degrees */
    public Ref rollRightDeg (double degrees) {
        return rotateDegX (degrees);
    }

    /** Rotate a number of degrees */
    public Ref turnDownDeg (double degrees) {
        return rotateDegY (degrees);
    }

    /** Rotate a number of degrees */
    public Ref turnUpDeg (double degrees) {
        return rotateDegY (-degrees);
    }

    /** Move relative to current position */
    public Ref forward (double dist) {
        return translate(dist,0.0,0.0);
    }
    /** Move relative to current position */
    public Ref backward (double dist) {
        return translate(-dist,0.0,0.0);
    }
    /** Move relative to current position */
    public Ref left (double dist) {
        return translate(0.0, dist, 0.0);
    }
    /** Move relative to current position */
    public Ref right (double dist) {
        return translate(0.0, -dist, 0.0);
    }
    /** Move relative to current position */
    public Ref up(double dist) {
        return translate(0.0, 0.0, dist);
    }
    /** Move relative to current position */
    public Ref down(double dist) {
        return translate(0.0, 0.0, -dist);
    }

    /** Scale unit vectors so that they get bigger by given factor.
    * Example: using a value of 1000 may change scale from metres to kilometres
    */
    public Ref scaleUp (double factor) {
        return scale(factor, factor, factor);
    }

    /** Scale unit vectors so that they get smaller by the given factor.
    * Example: using a value of 100 may change scale from metres to centimetres
    */
    public Ref scaleDown (double factor) {
        double actualFactor=1.0/factor;
        return scale(actualFactor, actualFactor, actualFactor);
    }

    public String toString() {
        return "pos=" + pos + " x=" + x + " y=" + y + " z=" + z;
    }

    public double getUnitVectorMaxLength() {
        double x=unit_x.length();
        double y=unit_y.length();
        double z=unit_z.length();
        double m=x;
        if (y>m) m=y;
        if (z>m) m=z;
        return m;
    }



}
