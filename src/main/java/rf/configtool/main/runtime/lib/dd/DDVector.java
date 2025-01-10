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

package rf.configtool.main.runtime.lib.dd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;

/**
* This class represents a 2d-vector.
*
* All vectors are immutable: operations on them always create new vectors.
*/
public class DDVector extends Obj {

    private Vector2d vec;
    
    public DDVector (Vector2d vec) {
        this.vec=vec;

        this.add(new FunctionAdd());
        this.add(new FunctionSub());
        this.add(new FunctionAngleDeg());
        this.add(new FunctionLength());
        this.add(new FunctionX());
        this.add(new FunctionY());
        this.add(new FunctionScaleTo());
        this.add(new FunctionScale());
    }
    
    public Vector2d getVec() {
        return vec;
    }

    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "DD.Vector";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "DD.Vector";
    }
    
    private DDVector self() {
        return this;
    }
        

 
    private Vector2d getVector (String name, List<Value> params, int pos, String err) throws Exception {
        Obj obj=getObj(name, params, pos);
        if (obj instanceof DDVector) return ((DDVector) obj).getVec();
        throw new Exception(err);
    }
    
    class FunctionAdd extends Function {
        public String getName() {
            return "add";
        }
        public String getShortDesc() {
            return "add(Vector2d) - add vector";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected Vec2d parameter");
            Vector2d vec=getVector("Vec2d", params, 0, "Expected Vec2d parameter");
            return new ValueObj(new DDVector(getVec().add(vec)));
        }
    }

    class FunctionSub extends Function {
        public String getName() {
            return "sub";
        }
        public String getShortDesc() {
            return "sub(Vector2d) - return vector that points from given vector point to THIS vector point";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected Vec2d parameter");
            Vector2d vec=getVector("Vec2d", params, 0, "Expected Vec2d parameter");
            
            return new ValueObj(new DDVector(getVec().intuitiveSub(vec)));
        }
    }


    class FunctionAngleDeg extends Function {
        public String getName() {
            return "angle";
        }
        public String getShortDesc() {
            return "angle(Vector2d) - get absolute angle in degrees between vectors (0-90)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected Vec2d parameter");
            Vector2d vec=getVector("Vec2d", params, 0, "Expected Vec2d parameter");
            return new ValueFloat(getVec().calcAbsoluteAngleDeg(vec));
        }
    }


    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - get length";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected  no parameters");
            return new ValueFloat(getVec().length());
        }
    }


    class FunctionX extends Function {
        public String getName() {
            return "x";
        }
        public String getShortDesc() {
            return "x() - get x component";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected  no parameters");
            return new ValueFloat(getVec().getX());
        }
    }


    class FunctionY extends Function {
        public String getName() {
            return "y";
        }
        public String getShortDesc() {
            return "y() - get y component";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected  no parameters");
            return new ValueFloat(getVec().getY());
        }
    }


    class FunctionScaleTo extends Function {
        public String getName() {
            return "scaleTo";
        }
        public String getShortDesc() {
            return "scaleTo(targetLength) - create vector of given length";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter targetLength");
            double targetLength=getFloat("targetLength", params, 0);
            return new ValueObj(new DDVector(getVec().scaleToLength(targetLength)));
        }
    }



    class FunctionScale extends Function {
        public String getName() {
            return "scale";
        }
        public String getShortDesc() {
            return "scale(xScale,yScale) - scale x and y (floats)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected parameters xScale, yScale (floats)");
            double xScale=getFloat("xScale", params, 0);
            double yScale=getFloat("yScale", params, 0);
            return new ValueObj(new DDVector(getVec().scale(xScale,yScale)));
        }
    }


}
