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

package rf.configtool.main.runtime;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.util.NumberFormat;

public class ValueInt extends Value implements IsSynthesizable {
    
    private long val;
    
    public ValueInt (long val) {
        this.val=val;
        Function[] arr={
                new FunctionBin(),
                new FunctionF(),
                new FunctionI(),
                new FunctionPow(),
                new FunctionHex(),
                new FunctionFmt(),
                new FunctionStr(),
            };
        setFunctions(arr);
    }
    
    protected ValueInt theObj() {
        return this;
    }
    
    public long getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "int";
    }

    @Override
    public String getValAsString() {
        return ""+val;
    }
    
    
    @Override 
    public String createCode() throws Exception {
        return getValAsString();
    }
    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true;
        if (v instanceof ValueInt) {
            return ((ValueInt) v).getVal()==val;
        }
        if (v instanceof ValueFloat) {
            return ((ValueFloat) v).getVal()==val;
        }
        return false;
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }


    
    class FunctionBin extends Function {
        public String getName() {
            return "bin";
        }
        public String getShortDesc() {
            return "bin() or bin(bits) - returns binary string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            int bits;
            if (params.size()==0) {
                bits=8;
            } else 
            if (params.size() == 1) {
                if (!(params.get(0) instanceof ValueInt)) {
                    throw new Exception("Expected optional parameter: bits (int)");
                }
                bits=(int) ((ValueInt) params.get(0)).getVal();
            } else {
                throw new Exception("Expected one optional parameter: bits (int)");
            }
            
            String s="";
            for (int i=0; i<bits; i++) {
                if (((val >> i) & 0x01) > 0) {
                    s="1"+s;
                } else {
                    s="0"+s;
                }
            }
            
            return new ValueString(s);
        }

    }
    
    class FunctionHex extends Function {
        public String getName() {
            return "hex";
        }
        public String getShortDesc() {
            return "hex() - returns hex string (positive values only)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String chars="0123456789abcdef";
            String result="";
            long remaining=val;
            while (remaining != 0) {
                int digit=(int) (remaining % 16);
                result=chars.charAt(digit)+result;
                remaining /= 16;
            }
            return new ValueString(result);
        }

    }
    
    class FunctionF extends Function {
        public String getName() {
            return "f";
        }
        public String getShortDesc() {
            return "f() - returns number as float";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(val);
        }

    }
    
    class FunctionI extends Function {
        public String getName() {
            return "i";
        }
        public String getShortDesc() {
            return "i() - returns as int (unchanged)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return theObj();
        }

    }
    
    class FunctionPow extends Function {
        public String getName() {
            return "pow";
        }
        public String getShortDesc() {
            return "pow(x) - returns value ^ x ";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected single parameter (int)");
            long x=getInt("x",params,0)-1;
            long result=val;
            for (int i=0; i<x; i++) {
                result*=val;
                
            }
            return new ValueInt(result);
        }

    }
    

    class FunctionFmt extends Function {
        public String getName() {
            return "fmt";
        }
        public String getShortDesc() {
            return "fmt(thousandSep) - returns String value on format xx,xxx,xxx for readability";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected parameter thousandSep");
            String thousandSep=getString("thousandSep",params,0);
            return new ValueString(NumberFormat.formatInt(val, thousandSep));
        }

    }
    
    class FunctionStr extends Function {
        public String getName() {
            return "str";
        }
        public String getShortDesc() {
            return "str - returns as string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueString(""+val);
        }

    }

}
