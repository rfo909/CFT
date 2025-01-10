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

public class Bounds2d {
    
    double xmin, xmax, ymin, ymax;

    public Bounds2d(ScreenCoordinates list[]) {
        this(list[0]);
        for (int i=0; i<list.length; i++) {
            addPoint(list[i]);
        }
    }
    
    public Bounds2d(ScreenCoordinates firstPoint) {
        xmin=xmax=firstPoint.getX();
        ymin=ymax=firstPoint.getY();
    }
    
    public Bounds2d(int x, int y) {
        xmin=xmax=x;
        ymin=ymax=y;
    }
    
    public void addPoint (ScreenCoordinates point) {
        double x=point.getX();
        double y=point.getY();
        if (x < xmin) xmin=x;
        else if (x > xmax) xmax=x;
        if (y < ymin) ymin=y;
        else if (y > ymax) ymax=y;
    }
    
    public void addPoint (int x, int y) {
        if (x < xmin) xmin=x;
        else if (x > xmax) xmax=x;
        if (y < ymin) ymin=y;
        else if (y > ymax) ymax=y;
    }
    
    public void addPoints (ScreenCoordinates list[]) {
        for (int i=0; i<list.length; i++) {
            addPoint(list[i]);
        }
    }
    
    public double getWidth() {
        return xmax-xmin;
    }
    
    public double getHeight() {
        return ymax-ymin;
    }
    
    public double getXMin() {
        return xmin;
    }
    
    public double getXMax() {
        return xmax;
    }
    
    public double getYMin() {
        return ymin;
    }
    
    public double getYMax() {
        return ymax;
    }
    
}
