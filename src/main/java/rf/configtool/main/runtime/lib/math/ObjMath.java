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

package rf.configtool.main.runtime.lib.math;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.dd.DD;

public class ObjMath extends Obj {
    
    public ObjMath() {
        this.add(new FunctionCos());
        this.add(new FunctionSin());
        this.add(new FunctionPI());
        this.add(new FunctionE());
        this.add(new FunctionSqrt());
        this.add(new FunctionLog());
        this.add(new FunctionLog10());
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


    class FunctionE extends Function {
        public String getName() {
            return "e";
        }
        public String getShortDesc() {
            return "e() - return e";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.E);
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
            if (params.size() != 1) throw new Exception("Expected value parameter");
            double value=getFloat("value", params, 0);
            return new ValueFloat(Math.sqrt(value));
        }
    }
    
    class FunctionLog extends Function {
        public String getName() {
            return "log";
        }
        public String getShortDesc() {
            return "log(value) - return log of value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected value parameter");
            double value=getFloat("value", params, 0);
            return new ValueFloat(Math.log(value));
        }
    }
    
    class FunctionLog10 extends Function {
        public String getName() {
            return "log10";
        }
        public String getShortDesc() {
            return "log10(value) - return log10 of value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected value parameter");
            double value=getFloat("value", params, 0);
            return new ValueFloat(Math.log10(value));
        }
    }
    
    // private helpers
    
    private double toRadians (double deg) {
        return 2*Math.PI * deg / 360.0;
    }
    
 }
