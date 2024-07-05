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

package rf.configtool.main.runtime.lib;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;

public class ObjData extends Obj {
    
    public ObjData() {
        Function[] arr={
                new FunctionFor(),
                new FunctionYes(),
                new FunctionEach(),
        };
        setFunctions(arr);
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Data";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Data";
    }
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionFor extends Function {
        public String getName() {
            return "for";
        }
        public String getShortDesc() {
            return "for(startValue,limit,increment) - returns list of values";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 3) throw new Exception("Expected three parameters");
            
            List<Value> result=new ArrayList<Value>();
            if (params.get(0) instanceof ValueFloat) {
                double val=getFloat("startValue", params, 0);
                double limit=getFloat("limit", params, 1);
                double incr=getFloat("increment", params, 2);
                
                double d=val;
                for (;;) {
                    if (incr > 0) {
                        if (d>=limit) break;
                    } else {
                        if (d<=limit) break;
                    }
                    result.add(new ValueFloat(d));
                    d += incr;
                }
                
            } else {
                long val=getInt("startValue", params, 0);
                long limit=getInt("limit", params, 1);
                long incr=getInt("increment", params, 2);

                long d=val;
                for (;;) {
                    if (incr > 0) {
                        if (d>=limit) break;
                    } else {
                        if (d<=limit) break;
                    }
                    result.add(new ValueInt(d));
                    d += incr;
                }
            }
                
            return new ValueList(result);
        }
    }

    class FunctionYes extends Function {
        public String getName() {
            return "yes";
        }
        public String getShortDesc() {
            return "yes(count,value) - returns list";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected two parameters");
            int count=(int) getInt("count", params, 0);
            Value v=params.get(1);
            
            List<Value> result=new ArrayList<Value>();
            for (int i=0; i<count; i++) result.add(v);
            return new ValueList(result);
        }
    }


    class FunctionEach extends Function {
        public String getName() {
            return "each";
        }
        public String getShortDesc() {
            return "each(from,to) - returns list of int values";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected two parameters");
            long from=getInt("from", params, 0);
            long to=getInt("to", params, 1);
            
            List<Value> result=new ArrayList<Value>();
            int incr=1;
            if (to < from) incr=-1;
            for(;;) {
                result.add(new ValueInt(from));
                if (from==to) break;
                from += incr;
            }
            return new ValueList(result);
        }
    }

    

}
