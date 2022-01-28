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

package rf.configtool.main.runtime.lib.dd;

import java.util.*;
import java.awt.Color;

/**
 * A polygon to be rendered, in global coordinates
 */
public class Polygon {
    private List<Vector2d> points;
    private Color color;
    private boolean linesOnly=false;
    
    public Polygon (List<Vector2d> points, Color color) {
        this.points=points;
        this.color = color;
    }
    
    public void setLinesOnly() {
        this.linesOnly=true;
    }

    
    public List<Vector2d> getPoints() {
        return points;
    }

    public Color getColor() {
        return color;
    }
    
    public boolean getLinesOnly() {
        return linesOnly;
    }

    public List<Line> getLines() {
        List<Line> lines=new ArrayList<Line>();
        for (int i=0; i<points.size()-1; i++) {
            lines.add(new Line(points.get(i), points.get(i+1), color));
        }
        return lines;
    }
    
    
}
