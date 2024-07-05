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

import java.awt.Color;

/**
 * A line to be rendered, in global coordinates
 */
public class Line {
    private Vector2d from, to;
    private Color color;
    
    public Line(Vector2d from, Vector2d to, Color color) {
        this.from = from;
        this.to = to;
        this.color = color;
    }

    public Vector2d getFrom() {
        return from;
    }

    public Vector2d getTo() {
        return to;
    }

    public Color getColor() {
        return color;
    }

    
    
}
