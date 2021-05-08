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

package rf.configtool.main.runtime;

import java.util.List;

import rf.configtool.main.Ctx;

public class ValueFloat extends Value {
    
    private double val;
    
    public ValueFloat (double val) {
        this.val=val;
		Function[] arr={
				new FunctionRound(),
				new FunctionF(),
				new FunctionI(),
				new FunctionFloor(),
				new FunctionLog(),
				new FunctionLog10(),
				new FunctionAbs(),
		};
		setFunctions(arr);
    }
    
    protected ValueFloat theObj() {
        return this;
    }
    
    public double getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "float";
    }

    @Override
    public String getValAsString() {
        return new java.math.BigDecimal(val).toPlainString();
    }
    
    @Override 
    public String synthesize() throws Exception {
        return getValAsString();
    }
    

    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true; // same instance
        if (v instanceof ValueFloat) {
            return (((ValueFloat) v).getVal()==val);
        }
        if (v instanceof ValueInt) {
            return (((ValueInt) v).getVal()==val);
        }
        return false; // otherwise, two floats are never quite equal
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }

    class FunctionRound extends Function {
        public String getName() {
            return "round";
        }
        public String getShortDesc() {
            return "round() - returns rounded int value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueInt((long) Math.round(val));
        }

    }
    
    class FunctionF extends Function {
        public String getName() {
            return "f";
        }
        public String getShortDesc() {
            return "f() - returns value as float (unchanged)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return theObj();
        }

    }

    class FunctionI extends Function {
        public String getName() {
            return "i";
        }
        public String getShortDesc() {
            return "i() - returns rounded int value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueInt((long) Math.round(val));
        }

    }
    
    class FunctionFloor extends Function {
        public String getName() {
            return "floor";
        }
        public String getShortDesc() {
            return "floor() - returns floored int value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueInt((long) Math.floor(val));
        }

    }
    
    class FunctionLog extends Function {
        public String getName() {
            return "log";
        }
        public String getShortDesc() {
            return "log()";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.log(val));
        }

    }
    
    class FunctionLog10 extends Function {
        public String getName() {
            return "log10";
        }
        public String getShortDesc() {
            return "log10()";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.log10(val));
        }

    }
    
    class FunctionAbs extends Function {
        public String getName() {
            return "abs";
        }
        public String getShortDesc() {
            return "abs() - returns abs value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.abs(val));
        }

    }
    


}
