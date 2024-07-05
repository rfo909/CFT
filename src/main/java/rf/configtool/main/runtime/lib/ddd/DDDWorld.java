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

package rf.configtool.main.runtime.lib.ddd;

import java.awt.Color;
import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjColor;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ddd.core.Triangle;
import rf.configtool.main.runtime.lib.ddd.core.TriangleReceiver;
import rf.configtool.main.runtime.lib.ddd.viewers.AreaViewer;

/**
 *
 */
public class DDDWorld extends Obj {
    
    private AreaViewer viewer;
    private TriangleReceiver triRecv;

    private void createViewer(double focalDist, double filmX, double filmY, int pixelsX, int pixelsY, Color defaultColor) {
        this.viewer=new AreaViewer(focalDist, filmX, filmY, pixelsX, pixelsY, defaultColor);
        this.triRecv=this.viewer; 
    }
    public DDDWorld() {
        // Defining camera in millimeters
        createViewer(35, 36, 24, 800, 600, Color.WHITE);
    
        this.add(new FunctionInit());
        this.add(new FunctionSetLightPos());
        this.add(new FunctionSetLightRange());
        this.add(new FunctionBrush());
        this.add(new FunctionOut());
        this.add(new FunctionRender());
        this.add(new FunctionSetMetallicReflection());
        this.add(new FunctionGetStats());

    }

    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "DDD";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DDD";
    }

    private DDDWorld self() {
        return this;
    }
    
    class FunctionInit extends Function {
        public String getName() {
            return "init";
        }

        public String getShortDesc() {
            return "init(focalDist,filmWidth,filmHeight,pixelsX,pixelsY,defaultColor) - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 6) throw new Exception("Expected six parameters");
            double focalDist=getFloat("focalDist",params,0);
            double filmWidth=getFloat("filmWidth",params,1);
            double filmHeight=getFloat("filmHeight",params,2);
            int pixelsX=(int) getInt("pixelsX", params, 3);
            int pixelsY=(int) getInt("pixelsY", params, 4);
            Obj col1=getObj("defaultColor", params, 5);
            if (col1 instanceof ObjColor) {
                Color defaultColor=((ObjColor) col1).getAWTColor();
                self().createViewer(focalDist, filmWidth, filmHeight, pixelsX, pixelsY, defaultColor);
                return new ValueObj(self());
            } else {
                throw new Exception("Expected six paramters");
            }
        }
    }
    
    class FunctionSetLightPos extends Function {
        public String getName() {
            return "setLightPos";
        }

        public String getShortDesc() {
            return "setLightPos(DDD.Ref) - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected DDD.Ref parameter");
            Obj obj=getObj("ref", params, 0);
            if (obj instanceof DDDRef) {
                self().viewer.setLightPos( ((DDDRef) obj).getRef() );
                return new ValueObj(self());
            } else {
                throw new Exception("Expected DDD.Ref parameter");
            }
        }
    }


    class FunctionSetLightRange extends Function {
        public String getName() {
            return "setLightRange";
        }

        public String getShortDesc() {
            return "setLightRange(Ref) - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected DDD.Ref parameter");
            Obj obj=getObj("ref", params, 0);
            if (obj instanceof DDDRef) {
                self().viewer.setLightReach( ((DDDRef) obj).getRef() );
                return new ValueObj(self());
            } else {
                throw new Exception("Expected DDD.Ref parameter");
            }
        }
    }



    
    class FunctionBrush extends Function {
        public String getName() {
            return "Brush";
        }

        public String getShortDesc() {
            return "Brush() - create 3D Brush object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            return new ValueObj(new DDDBrush(triRecv));
        }
    }


    
    class FunctionRender extends Function {
        public String getName() {
            return "render";
        }

        public String getShortDesc() {
            return "render(file) - render as PNG to file";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected File parameter");
            Obj file=getObj("file",params,0);
            if (file instanceof ObjFile) {
                File f=((ObjFile) file).getFile();
                self().viewer.writePNG(f);
                return new ValueObj(self());
            } else {
                throw new RuntimeException("Expected File parameter");
            }
        }
    }


    class FunctionOut extends Function {
        public String getName() {
            return "out";
        }

        public String getShortDesc() {
            return "out(Triangle) - add triangle to scene";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected Triangle parameter");
            Obj tri=getObj("triangle",params,0);
            if (tri instanceof DDDTriangle) {
                Triangle t=((DDDTriangle) tri).getTri();
                self().viewer.tri(t);
                return new ValueObj(self());
            } else {
                throw new RuntimeException("Expected Triangle parameter");
            }
        }
    }


    class FunctionSetMetallicReflection extends Function {
        public String getName() {
            return "setMetallicReflection";
        }

        public String getShortDesc() {
            return "setMetallicReflection(bool) - modify influence from light source - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected boolean parameter");
            boolean bool=getBoolean("bool", params, 0);
            self().viewer.setMetallicReflection(bool);
            return new ValueObj(self());
        }
    }
    

    class FunctionGetStats extends Function {
        public String getName() {
            return "getStats";
        }

        public String getShortDesc() {
            return "getStats() - returns Dict";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new RuntimeException("Expected no parameters");
            ObjDict dict=new ObjDict();
            dict.set("triCount", new ValueInt(viewer.getTriCount())); 
            dict.set("triPaintedCount", new ValueInt(viewer.getTriPaintedCount()));   
            return new ValueObj(dict);
        }
    }
    

    
}
