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

package rf.configtool.main.runtime.lib.dd;


/**
 * This class is a 2d-adaption of the original 3d ref-class.
 *
 * 2001-05-01 RF
 */
public class Ref {

    // default unit vectors
    protected static final Vector2d unit_x = new Vector2d(1, 0);
    protected static final Vector2d unit_y = new Vector2d(0, 1);

    private Vector2d pos;
    private Vector2d x; // current unit vectors
    private Vector2d y;

    public Ref (Vector2d pos, Vector2d x, Vector2d y) {
        this.pos=pos;
        this.x=x;
        this.y=y;
    }
    
    public Ref () {
        pos=new Vector2d(0,0);
        x=unit_x;
        y=unit_y;
    }

    
    // ------------------------------------------------------------------------------
    // Get-methods
    // ------------------------------------------------------------------------------

    /**
     * Returns absolute position of reference system, expressed as vector in world
     * coordinates
     */
    public Vector2d getPos() {
        return pos; // may return the original since Vector3d objects are immutable
    }

    /** Returns absolute length of unit vector, expressed in world measures */
    public double getUnitXLength() {
        return x.length();
    }

    /** Returns absolute length of unit vector, expressed in world measures */
    public double getUnitYLength() {
        return y.length();
    }

    /** Return biggest absolute length of unit vector */
    public double getUnitVectorMaxLength() {
        double lx = getUnitXLength();
        double ly = getUnitYLength();
        double result = lx;
        if (ly > result)
            result = ly;

        return result;
    }

    // --------------------------------------------------------------
    // Core operations: rotation in xy-plane
    // --------------------------------------------------------------

    /** Creates new rotated Ref */
    public Ref rotate(double radians) {
        Vector2d nx = unit_x.rotate(radians).transform(x, y);
        Vector2d ny = unit_y.rotate(radians).transform(x, y);
        return new Ref(pos, nx, ny);
    }

    // --------------------------------------------------------------
    // Core operations: translate along local unit-vectors
    // --------------------------------------------------------------

    /** Creates new Ref with relative position as given by parameters */
    public Ref translate(double distx, double disty) {
        Vector2d newPos = pos.add(x.mul(distx)).add(y.mul(disty));
        return new Ref(newPos, x, y);
    }

    // --------------------------------------------------------------
    // Core operations: scaling unit vectors
    // --------------------------------------------------------------

    /**
     * Creates new Ref with unit vectors scaled as given.
     */
    public Ref scale(double factorx, double factory) {
        return new Ref(pos, x.mul(factorx), y.mul(factory));
    }

    public double getScaleFactor() {
        return x.length();
    }

    public Ref setScaleFactor(double factor) {
        Vector2d nx=x.scaleToLength(factor);
        Vector2d ny=y.scaleToLength(factor);
        return new Ref(this.pos,nx,ny);
    }

    // --------------------------------------------------------------
    //
    // Utility methods
    //
    // (these all use the core operations to do the actual work)
    // --------------------------------------------------------------

    /** Creates new Ref with relative position as given by vector */
    public Ref translate(Vector2d v) {
        return translate(v.getX(), v.getY());
    }

    public Ref rotateDeg(double degrees) {
        return rotate(Math.PI * degrees / 180.0);
    }

    /** Turn a fraction of the circle */
    public Ref turnLeftFraction(double fraction) {
        return rotate(fraction * Math.PI * 2);
    }

    /**
     * Turn a fraction of the circle, the fraction being (step/totalSteps).
     * Convenience method
     */
    public Ref turnLeftFraction(int step, int totalSteps) {
        double fraction = ((double) step) / ((double) totalSteps);
        return turnLeftFraction(fraction);
    }

    /** Turn a fraction of the circle */
    public Ref turnRightFraction(double fraction) {
        return rotate(-(fraction * Math.PI * 2));
    }

    /**
     * Turn a fraction of the circle, the fraction being (step/totalSteps).
     * Convenience method
     */
    public Ref turnRightFraction(int step, int totalSteps) {
        double fraction = ((double) step) / ((double) totalSteps);
        return turnRightFraction(fraction);
    }

    /** Rotate a number of degrees */
    public Ref turnLeftDeg(double degrees) {
        return rotateDeg(degrees);
    }

    /** Rotate a number of degrees */
    public Ref turnRightDeg(double degrees) {
        return rotateDeg(-degrees);
    }

    /** Move relative to current position */
    public Ref forward(double dist) {
        return translate(dist, 0.0);
    }

    /** Move relative to current position */
    public Ref backward(double dist) {
        return translate(-dist, 0.0);
    }

    /** Move relative to current position */
    public Ref left(double dist) {
        return translate(0.0, dist);
    }

    /** Move relative to current position */
    public Ref right(double dist) {
        return translate(0.0, -dist);
    }

    /**
     * Scale unit vectors so that they get bigger by given factor. Example: using a
     * value of 1000 may change scale from metres to kilometres
     */
    public Ref scaleUp(double factor) {
        return scale(factor, factor);
    }

    /**
     * Scale unit vectors so that they get smaller by the given factor. Example:
     * using a value of 100 may change scale from metres to centimetres
     */
    public Ref scaleDown(double factor) {
        double actualFactor = 1.0 / factor;
        return scale(actualFactor, actualFactor);
    }

    public String toString() {
        return "pos=" + pos + " x=" + x + " y=" + y;
    }

    
    public Vector2d transformLocalToGlobal (Vector2d localVec) {
        Vector2d convertedToLocalSystem = x.mul(localVec.getX()).add(y.mul(localVec.getY()));
        return pos.add(convertedToLocalSystem);
    }

}
