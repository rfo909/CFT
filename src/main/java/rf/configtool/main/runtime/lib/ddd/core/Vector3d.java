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

package rf.configtool.main.runtime.lib.ddd.core;

/**
* This class represents a 3d-vector.
*
* All vectors are immutable: operations on them always create new vectors.
*/
public class Vector3d {
    private double x,y,z;

    public Vector3d () {
        x=y=z=0.0;
    }

    public Vector3d (double x, double y, double z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public Vector3d (int x, int y, int z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public Vector3d (Vector3d v) {
        this.x=v.getX();
        this.y=v.getY();
        this.z=v.getZ();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    /** Create new vector that is the result of adding another vector to this vector */
    public Vector3d add(Vector3d v) {
        return new Vector3d(x+v.x, y+v.y, z+v.z);
    }

    /**
    * Transform this vector to new coordinate system by
    * multiplying its components with the given unit vectors, adding the
    * result to create a new vector that is local to the system in which the
    * unit vectors are expressed.
    */
    public Vector3d transform (Vector3d unitx, Vector3d unity, Vector3d unitz) {
        return unitx.mul(x).add(unity.mul(y)).add(unitz.mul(z));
    }
    /** Create new vector represents the line from the end of this vector to the end
    * of the given vector: this is the subtraction operation, "v" minus "this". It is
    * the opposite of the add() method, in that adding the result of this method to "this"
    * reproduces the argument "v". 
    */
    public Vector3d sub(Vector3d v) {
        // Used in viewer calculations, so do not change - created intuitiveSub for CFT
        return new Vector3d(v.x-x, v.y-y, v.z-z);
    }

    public Vector3d intuitiveSub (Vector3d v) {
        return new Vector3d(x-v.x, y-v.y, z-v.z);
    }
    
    /**
    * Calculate angle (in radians) between this vector and the given vector. It is
    * absolute in that it is in the range 0..pi/2, (0-90 deg).
    */
    public double calcAbsoluteAngleRadians (Vector3d v) {
        double u1=x;
        double u2=y;
        double u3=z;

        double v1=v.x;
        double v2=v.y;
        double v3=v.z;

        double dotProduct=u1*v1 + u2*v2 + u3*v3;

        double cosAngle=dotProduct / (length()*v.length());
        cosAngle=Math.abs(cosAngle);        // only interested in 0..pi/2

        double angle=Math.acos(cosAngle);

        if (angle < 0.0 || angle > Math.PI/2.0) {
            System.out.println("unexpected result from acos: " + angle);
        }

        return angle;

    }

    /**
    * Calculate angle between this vector and the given vector. The result is
    * a floating point number in the range 0-1, where 0.0 is parallell and 1.0 is
    * 90 degrees angle.
    */
    public double calcAbsoluteAngleNormalized (Vector3d v) {
        return 2.0 * calcAbsoluteAngleRadians(v) / Math.PI;
    }



    /** Create new vector that is this vector multiplied by a factor */
    public Vector3d mul(double factor) {
        return new Vector3d (x*factor, y*factor, z*factor);
    }

    /** Calculate absolute length of vector */
    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    /** Create new vector that is this vector rotated about the x axis. Angle given in radians. */
    public Vector3d rotateX (double radians) {
        double cosFactor=Math.cos(radians);
        double sinFactor=Math.sin(radians);
        return new Vector3d (
            x,
            y*cosFactor - z*sinFactor,
            y*sinFactor + z*cosFactor);
    }

    /** Create new vector that is this vector rotated about the y axis. Angle given in radians. */
    public Vector3d rotateY (double radians) {
        double cosFactor=Math.cos(radians);
        double sinFactor=Math.sin(radians);
        return new Vector3d (
            z*sinFactor + x*cosFactor,
            y,
            z*cosFactor - x*sinFactor);
    }

    /** Create new vector that is this vector rotated about the z axis. Angle given in radians. */
    public Vector3d rotateZ (double radians) {
        double cosFactor=Math.cos(radians);
        double sinFactor=Math.sin(radians);
        return new Vector3d (
            x*cosFactor - y*sinFactor,
            x*sinFactor + y*cosFactor,
            z);
    }

    /** Create new vector that is this vector rotated around an axis. Angle given in degrees. */
    public Vector3d rotateDegX (double degrees) {
        return rotateX (Math.PI * degrees / 180.0);
    }

    /** Create new vector that is this vector rotated around an axis. Angle given in degrees. */
    public Vector3d rotateDegY (double degrees) {
        return rotateY (Math.PI * degrees / 180.0);
    }

    /** Create new vector that is this vector rotated around an axis. Angle given in degrees. */
    public Vector3d rotateDegZ (double degrees) {
        return rotateZ (Math.PI * degrees / 180.0);
    }

    /** Rotate a fraction of the full circle (0..1) */
    public Vector3d rotateFractX (double fraction) {
        return rotateX(fraction * Math.PI * 2);
    }

    /** Rotate a fraction of the full circle (0..1) */
    public Vector3d rotateFractY (double fraction) {
        return rotateY(fraction * Math.PI * 2);
    }

    /** Rotate a fraction of the full circle (0..1) */
    public Vector3d rotateFractZ (double fraction) {
        return rotateZ(fraction * Math.PI * 2);
    }




    private String fmt (double d) {
        String s=""+d;
        StringBuffer sb=new StringBuffer();
        boolean foundDot=false;
        int decCount=2;
        for (int i=0; i<s.length(); i++) {
            char c=s.charAt(i);
            sb.append(c);
            if (foundDot) decCount--;
            if (decCount<=0) break;
            if (c=='.') foundDot=true;
        }
        return sb.toString();
    }
    
    
    public String toString() {
        return "[" + fmt(x) + ", " + fmt(y) + ", " + fmt(z) + "]";
    }
}
