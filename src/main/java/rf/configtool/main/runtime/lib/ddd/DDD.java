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

package rf.configtool.main.runtime.lib.ddd;

import java.awt.Color;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjColor;
import rf.configtool.main.runtime.lib.ddd.core.Triangle;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;
import rf.configtool.main.runtime.lib.ddd.core.VisibleAttributes;

/**
 * Publicly known object via Lib
 *
 */
public class DDD extends Obj {
    

    public DDD() {
        this.add(new FunctionRef());
        this.add(new FunctionVector());
        this.add(new FunctionWorld());
        this.add(new FunctionRTriangle());
        this.add(new FunctionVTriangle());
        this.add(new FunctionBezier());
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

    private DDD self() {
        return this;
    }

    class FunctionRef extends Function {
        public String getName() {
            return "Ref";
        }

        public String getShortDesc() {
            return "Ref() - create DDD.Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            return new ValueObj(new DDDRef());
        }
    }

    
  

    class FunctionVector extends Function {
        public String getName() {
            return "Vector";
        }

        public String getShortDesc() {
            return "Vector(x,y,z) - returns DDD.Vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 3) throw new RuntimeException("Expected parameters x,y,z");
            double x=getFloat("x", params, 0);
            double y=getFloat("y", params, 1);
            double z=getFloat("z", params, 2);
            Vector3d vec=new Vector3d(x,y,z);
            return new ValueObj(new DDDVector(vec));
        }
    }
    
    class FunctionWorld extends Function {
        public String getName() {
            return "World";
        }

        public String getShortDesc() {
            return "World() - create scene object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            return new ValueObj(new DDDWorld());
        }
    }
    
    class FunctionRTriangle extends Function {
        public String getName() {
            return "RTriangle";
        }

        public String getShortDesc() {
            return "RTriangle(a,b,c,color) - a,b,c are Ref objects";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 4) throw new RuntimeException("Expected Ref parameters a,b,c + color");
            Obj a1=getObj("a",params,0);
            Obj b1=getObj("b",params,1);
            Obj c1=getObj("c",params,2);
            Obj col1=getObj("color",params,3);
            
            if (!(a1 instanceof DDDRef) || !(b1 instanceof DDDRef) || !(c1 instanceof DDDRef) || !(col1 instanceof ObjColor)) {
                throw new RuntimeException("Expected Ref parameters a,b,c + color");
            }
            Vector3d a=((DDDRef) a1).getRef().getPos();
            Vector3d b=((DDDRef) b1).getRef().getPos();
            Vector3d c=((DDDRef) c1).getRef().getPos();
            
            Color color=((ObjColor) col1).getAWTColor();
            Triangle t=new Triangle(a,b,c,new VisibleAttributes(color));
            return new ValueObj(new DDDTriangle(t));
        }
    }
    

 
    class FunctionVTriangle extends Function {
        public String getName() {
            return "VTriangle";
        }

        public String getShortDesc() {
            return "VTriangle(a,b,c,color) - a,b,c are 3d Vectors";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 4) throw new RuntimeException("Expected Vector parameters a,b,c + color");
            Obj a1=getObj("a",params,0);
            Obj b1=getObj("b",params,1);
            Obj c1=getObj("c",params,2);
            Obj col1=getObj("color",params,3);
            
            if (!(a1 instanceof DDDVector) || !(b1 instanceof DDDVector) || !(c1 instanceof DDDVector) || !(col1 instanceof ObjColor)) {
                throw new RuntimeException("Expected Ref parameters a,b,c + color");
            }
            Vector3d a=((DDDVector) a1).getVec();
            Vector3d b=((DDDVector) b1).getVec();
            Vector3d c=((DDDVector) c1).getVec();
            
            Color color=((ObjColor) col1).getAWTColor();
            Triangle t=new Triangle(a,b,c,new VisibleAttributes(color));
            return new ValueObj(new DDDTriangle(t));
        }
    }
    

    class FunctionBezier extends Function {
        public String getName() {
            return "Bezier";
        }

        public String getShortDesc() {
            return "Bezier() - create object for calculating bezier curve";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            return new ValueObj(new DDDBezier());
        }
    }

 
    
}
