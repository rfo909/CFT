/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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


/**
* Container-class, defines x and y. These are actually floating-point values,
* but may be converted to int as well.
*/
public class ScreenCoordinates {
    
    private double x, y;
    
    public ScreenCoordinates (int x, int y) {
        this.x=x;
        this.y=y;
    }
    
    public ScreenCoordinates (double x, double y) {
        this.x=x;
        this.y=y;
    }
    
    public int getIntX() {
        return (int) x;
    }
    
    public int getIntY() {
        return (int) y;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
}
