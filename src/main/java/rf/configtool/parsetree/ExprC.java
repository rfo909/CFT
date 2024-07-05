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

package rf.configtool.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueString;

public class ExprC extends ExprCommon {
    private String[] sep= {"+","-"};

    private List<ExprD> parts=new ArrayList<ExprD>();
    private List<String> separators=new ArrayList<String>();
    
    public ExprC (TokenStream ts) throws Exception {
        super(ts);
        for (;;) {
            parts.add(new ExprD(ts));
            String x=Expr.matchSeparator(ts,sep);
            if (x != null) {
                separators.add(x);
            } else {
                break;
            }
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        if (parts.size() == 1) {
            return parts.get(0).resolve(ctx);
        }
        
        // at least two parts
        Value currVal=parts.get(0).resolve(ctx);
        
        for (int pos=1; pos<parts.size(); pos++) {
            String sep=separators.get(pos-1);
            Value nextVal=parts.get(pos).resolve(ctx);
            
            currVal=combine(currVal, sep, nextVal);
        }
        return currVal;
    }
     
    // int +- int -> int
    // int +- float -> float
    // float +- int/float -> float
    // list +- list -> list
    // string + any -> string
    private Value combine (Value a, String sep, Value b) throws Exception {
        if (a instanceof ValueInt) {
            long x1=((ValueInt) a).getVal();
            if (b instanceof ValueInt) {
                long x2=((ValueInt) b).getVal();
                return new ValueInt(calc(x1, sep, x2));
            } else if (b instanceof ValueFloat) {
                double x2=((ValueFloat) b).getVal();
                return new ValueFloat(calc(x1, sep, x2));
            } else {
                throw ex("Expected int " + sep + " int/float");
            }
        }
        if (a instanceof ValueFloat) {
            double x1=((ValueFloat) a).getVal();
            double x2;
            if (b instanceof ValueInt) {
                x2=((ValueInt) b).getVal();
            } else if (b instanceof ValueFloat) {
                x2=((ValueFloat) b).getVal();
            } else {
                throw ex("Expected float " + sep + " int/float");
            }
            return new ValueFloat(calc(x1,sep,x2));
        }
        if (a instanceof ValueList) {
            ValueList x1=(ValueList) a;
            ValueList x2;
            if (b instanceof ValueList) {
                x2=(ValueList) b;
            } else {
                List<Value> tmp=new ArrayList<Value>();
                tmp.add(b);
                x2=new ValueList(tmp);
            }
            return calc(x1,sep,x2);
        }
        if (a instanceof ValueString) {
            String x1=((ValueString) a).getVal();
            String x2=b.getValAsString();
            return new ValueString(calc(x1,sep,x2));
        }
        throw ex("Invalid value " + a.getDescription() + " " + sep + " " + b.getDescription());

    }
    
    private long calc (long a, String sep, long b) throws Exception {
        if (sep.equals("+")) return a+b;
        if (sep.equals("-")) return a-b;
        throw ex("Internal error: invalid separator");
    }
    private double calc (double a, String sep, double b) throws Exception {
        if (sep.equals("+")) return a+b;
        if (sep.equals("-")) return a-b;
        throw ex("Internal error: invalid separator");
    }
    private ValueList calc (ValueList a, String sep, ValueList b) throws Exception {
        boolean add;
        if (sep.equals("+")) {
            return a.addElements(b);
        } else if (sep.equals("-")) {
            return a.removeElements(b);
        } else {
            throw ex("Internal error: invalid separator");
        }
    }
    private String calc (String a, String sep, String b) throws Exception {
        if (sep.equals("+")) {
            return a+b;
        } else if (sep.equals("-")) {
            throw ex("Expected string + string");
        } else {
            throw ex("Internal error: invalid separator");
        }
    }

}
