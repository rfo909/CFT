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

package rf.configtool.main.runtime.lib.dd;

import java.awt.Color;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjColor;


/**
 * The 2D brush is simply for drawing lines, nothing fancy
 */
public class DDBrush extends Obj {

    private ViewReceiver recv;
    private Color color;
    private boolean linesOnly=false;

    private Vector2d offsetA, offsetB;
    
    private Vector2d prevA, prevB;

    
    /**
     * As there are problems with right being negative and left being positibe, we mask this by
     * not using vectors in the call interface for creating a brush, but instead signed
     * offsets, which are positive to the right and negative to the left.
     * @param recv
     * @param offsetA
     * @param offsetB
     */
    public DDBrush (ViewReceiver recv, double offsetA, double offsetB) {
        this.recv=recv;
        this.offsetA=new Vector2d(0,-offsetA);
        this.offsetB=new Vector2d(0,-offsetB);
        this.color=new Color(0,0,0);
        
        this.add(new FunctionPenDown());
        this.add(new FunctionPenUp());
        this.add(new FunctionSetColor());
        this.add(new FunctionSetLinesOnly());
        
    }
    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    @Override
    public String getTypeName() {
        return "DD.Brush";
    }

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DD.Brush";
    }

    private DDBrush self() {
        return this;
    }

    
       class FunctionPenDown extends Function {
            public String getName() {
                return "penDown";
            }

            public String getShortDesc() {
                return "penDown(Ref) - draw area from last penDown";
            }

            public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
                if (params.size() != 1) throw new RuntimeException("Expected Ref parameter");
                
                Obj ref1=getObj("Ref",params,0);
                if (ref1 instanceof DDRef) {
                    Ref ref=((DDRef) ref1).getRef();
                    // calculate new global points a and b
                    Vector2d a=ref.transformLocalToGlobal(offsetA);
                    Vector2d b=ref.transformLocalToGlobal(offsetB);
                    
                    if (prevA != null && prevB != null) {
                        List<Vector2d> points=new ArrayList<Vector2d>();
                        points.add(prevA);
                        points.add(prevB);
                        points.add(b);
                        points.add(a);
                        points.add(prevA); // closing polygon
                        
                        Polygon poly=new Polygon(points,color);
                        if (linesOnly) poly.setLinesOnly();
                        recv.addPolygon(poly);
                        
//                      StringBuffer sb=new StringBuffer();
//                      for (Vector2d p:points) {
//                          sb.append(" " + p.toString());
//                      }
//                      System.out.println(sb.toString());
                    }
                    prevA=a;
                    prevB=b;
                    return new ValueObj(self());
                } else {
                    throw new RuntimeException("Expected Ref parameter");
                }
            }
        }

    
       class FunctionPenUp extends Function {
            public String getName() {
                return "penUp";
            }

            public String getShortDesc() {
                return "penUp() - stop drawing";
            }

            public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
                if (params.size() != 0) throw new RuntimeException("Expected no parameters");
                prevA=prevB=null;
                return new ValueObj(self());
            }
        }

    
       class FunctionSetColor extends Function {
            public String getName() {
                return "setColor";
            }

            public String getShortDesc() {
                return "setColor(Color) - set color";
            }

            public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
                if (params.size() != 1) throw new RuntimeException("Expected Color parameter");
                Obj col1=getObj("Color",params,0);
                if (col1 instanceof ObjColor) {
                    Color color=((ObjColor) col1).getAWTColor();
                    self().color=color;
                    return new ValueObj(self());
                } else {
                    throw new RuntimeException("Expected Color parameter");
                }
            }
        }
       
       
       class FunctionSetLinesOnly extends Function {
            public String getName() {
                return "setLinesOnly";
            }

            public String getShortDesc() {
                return "setLinesOnly() - render brush polygons as lines only, not filled";
            }

            public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
                if (params.size() != 0) throw new RuntimeException("Expected no parameters");
                self().linesOnly=true;
                return new ValueObj(self());
            }
        }
}
