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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;



/**
 * This class is a 2d-adaption of the original 3d ref-class.
 *
 * 2001-05-01 RF
 */
public class DDRef extends Obj {

    private Ref ref;
    private DDWorld world;
    
    public Ref getRef() {
        return ref;
    }
    public DDRef(Ref ref, DDWorld world) {
        this.ref=ref;
		this.world=world;
		
		this.add(new FunctionWorld());
		        
        this.add(new FunctionScaleUp());
        this.add(new FunctionScaleDown());
        
        this.add(new FunctionTurnLeft());
        this.add(new FunctionTurnRight());
        
        this.add(new FunctionFwd());
        this.add(new FunctionBack());
        this.add(new FunctionLeft());
        this.add(new FunctionRight());
        
        this.add(new FunctionTranslate());
        
        this.add(new FunctionGetPosVector());
        this.add(new FunctionGetScaleFactor());
        this.add(new FunctionSetScaleFactor());
        this.add(new FunctionGetTransformedVector());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    @Override
    public String getTypeName() {
        return "DD.Ref";
    }

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DD.Ref";
    }

    private DDRef self() {
        return this;
    }

    class FunctionWorld extends Function {
        public String getName() {
            return "world";
        }

        public String getShortDesc() {
            return "world() - get world object given when Ref was created";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new RuntimeException("Expected no parameters");
            if (world != null) {
            	return new ValueObj(world);
            } else {
            	return new ValueNull();
            }
        }
    }
    
    class FunctionScaleUp extends Function {
        public String getName() {
            return "scaleUp";
        }

        public String getShortDesc() {
            return "scaleUp(factor) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected factor parameter");
            double factor=getFloat("factor", params, 0);
            
            return new ValueObj(new DDRef(getRef().scaleUp(factor), world));
        }
    }


    class FunctionScaleDown extends Function {
        public String getName() {
            return "scaleDown";
        }

        public String getShortDesc() {
            return "scaleDown(factor) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected factor parameter");
            double factor=getFloat("factor", params, 0);
            
            return new ValueObj(new DDRef(getRef().scaleDown(factor), world));
        }
    }
    
    
    
    class FunctionTurnLeft extends Function {
        public String getName() {
            return "turnLeft";
        }

        public String getShortDesc() {
            return "turnLeft(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
            double degrees=getFloat("degrees", params, 0);
            
            return new ValueObj(new DDRef(getRef().turnLeftDeg(degrees), world));
        }
    }
    
    
    
    class FunctionTurnRight extends Function {
        public String getName() {
            return "turnRight";
        }

        public String getShortDesc() {
            return "turnRight(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
            double degrees=getFloat("degrees", params, 0);
            
            return new ValueObj(new DDRef(getRef().turnRightDeg(degrees), world));
        }
    }
    
    
    
    class FunctionFwd extends Function {
        public String getName() {
            return "fwd";
        }

        public String getShortDesc() {
            return "fwd(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
            double dist=getFloat("dist", params, 0);
            
            return new ValueObj(new DDRef(getRef().forward(dist), world));
        }
    }


    class FunctionBack extends Function {
        public String getName() {
            return "back";
        }

        public String getShortDesc() {
            return "back(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
            double dist=getFloat("dist", params, 0);
            
            return new ValueObj(new DDRef(getRef().backward(dist), world));
        }
    }


    class FunctionLeft extends Function {
        public String getName() {
            return "left";
        }

        public String getShortDesc() {
            return "left(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
            double dist=getFloat("dist", params, 0);
            
            return new ValueObj(new DDRef(getRef().left(dist), world));
        }
    }


    class FunctionRight extends Function {
        public String getName() {
            return "right";
        }

        public String getShortDesc() {
            return "right(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
            double dist=getFloat("dist", params, 0);
            
            return new ValueObj(new DDRef(getRef().right(dist), world));
        }
    }

    
    class FunctionTranslate extends Function {
        public String getName() {
            return "translate";
        }

        public String getShortDesc() {
            return "translate(vec) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected 2d vector parameter");

            Obj vec1=getObj("vec",params,0);
            if (vec1 instanceof DDVector) {
                Vector2d vec=((DDVector) vec1).getVec();
                Ref newRef=getRef().translate(vec.getX(), vec.getY());
                return new ValueObj(new DDRef(newRef, world));
            } else {
                throw new RuntimeException("Expected 3D vector parameter");
            }
        }
    }

    
    class FunctionGetPosVector extends Function {
        public String getName() {
            return "getPosVector";
        }

        public String getShortDesc() {
            return "getPosVector() - get position vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new RuntimeException("Expected no parameters");
            Vector2d pos=getRef().getPos();
            return new ValueObj(new DDVector(pos));
        }
    }


    class FunctionGetScaleFactor extends Function {
        public String getName() {
            return "getScaleFactor";
        }

        public String getShortDesc() {
            return "getScaleFactor() - get current scale factor";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new RuntimeException("Expected no parameters");
            double factor=getRef().getScaleFactor();
            return new ValueFloat(factor);
        }
    }


    class FunctionGetTransformedVector extends Function {
        public String getName() {
            return "getTransformedVector";
        }

        public String getShortDesc() {
            return "getTransformedVector(vec) - transform local vector inside Ref system, to global vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected 3D vector parameter");
            Obj vec1=getObj("vec",params,0);
            if (vec1 instanceof DDVector) {
                Vector2d vec=((DDVector) vec1).getVec();
                Vector2d result=getRef().transformLocalToGlobal(vec);
                return new ValueObj(new DDVector(result));
            } else {
                throw new RuntimeException("Expected 3D vector parameter");
            }
        }
    }


    
    class FunctionSetScaleFactor extends Function {
        public String getName() {
            return "setScaleFactor";
        }

        public String getShortDesc() {
            return "setScaleFactor(double) - set current scale factor - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected scaleFactor parameter");
            double factor=getFloat("factor", params, 0);
            return new ValueObj(new DDRef(getRef().setScaleFactor(factor), world));
        }
    }

}
