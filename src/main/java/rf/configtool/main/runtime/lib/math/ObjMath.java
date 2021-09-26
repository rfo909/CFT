/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

package rf.configtool.main.runtime.lib.math;

import java.io.*;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

public class ObjMath extends Obj {
    
    public ObjMath() {
        this.add(new FunctionCos());
        this.add(new FunctionSin());
        this.add(new FunctionPI());
        this.add(new FunctionSqrt());
        this.add(new FunctionDD());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Math";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Math";
    }
    
    private Obj theObj () {
        return this;
    }
    

    
    class FunctionCos extends Function {
        public String getName() {
            return "cos";
        }
        public String getShortDesc() {
            return "cos(degrees) - cosine of angle in degrees";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected degrees parameter");
            double deg=getFloat("degrees", params,0);
            double result=Math.cos(toRadians(deg));
            return new ValueFloat(result);
        }
    }
    
    class FunctionSin extends Function {
        public String getName() {
            return "sin";
        }
        public String getShortDesc() {
            return "sin(degrees) - sine of angle in degrees";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected degrees parameter");
            double deg=getFloat("degrees", params,0);
            double result=Math.sin(toRadians(deg));
            return new ValueFloat(result);
        }
    }
    
    class FunctionPI extends Function {
        public String getName() {
            return "PI";
        }
        public String getShortDesc() {
            return "PI() - return Pi";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.PI);
        }
    }
    
    class FunctionSqrt extends Function {
        public String getName() {
            return "sqrt";
        }
        public String getShortDesc() {
            return "sqrt(value) - return square root";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected value parameters");
            double value=getFloat("value", params, 0);
            return new ValueFloat(Math.sqrt(value));
        }
    }
    
    // private helpers
    
    private double toRadians (double deg) {
        return 2*Math.PI * deg / 360.0;
    }
    
    class FunctionDD extends Function {
        public String getName() {
            return "DD";
        }
        public String getShortDesc() {
            return "DD() - create object for 2D (vector) calculations";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjDD());
        }
    }
}
