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

/**
* This class identifies a triangle, which is the basic unit for
* rendering.
*/
public class Triangle extends Plane3d {

    private VisibleAttributes attr;

    /**
    * Instantiate a triangle from three points, as identified by these vectors
    * from the eye (0,0,0) to the point. For the plane equation to be of any use
    * in deciding whether a point is on the inside or outside of the plane, the
    * points must be given in an anti-clockwise order seen from the outside.
    * Outside is identified by (Ax+By+Cz+D > 0) for a point [x,y,z].
    */
    public Triangle (Vector3d a, Vector3d b, Vector3d c, VisibleAttributes attr) {
        super(a,b,c);
        this.attr=attr;
    }

    public VisibleAttributes getAttributes() {
        return attr;
    }
    
    
}
