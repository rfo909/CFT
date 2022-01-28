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
* This interface represents anyone that is capable of processing the triangles
* that are generated. 
*/
public interface TriangleReceiver {

    /**
    * A triangle has been generated
    */
    public void tri (Triangle t) ;

    /**
    * This method returns true if the object whose bounds are given as parameter
    * will definitely not be displayed in the viewer (if TriangleReceiver is a viewer),
    * or otherwise should not be calculated as triangles.
    * <p>
    * Composite objects at all levels can call this method with their own bounds, to 
    * effectively prune the world tree fo systems and subsystems.
    */
    public boolean notVisible (rf.configtool.main.runtime.lib.ddd.core.Bounds3d bounds) ;
    

}
