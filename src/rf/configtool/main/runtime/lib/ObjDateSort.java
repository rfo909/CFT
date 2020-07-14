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

package rf.configtool.main.runtime.lib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

/**
 * Sorting lines starting with date/time is typical for processing log files.
 * Even though doing so is also possible via the Int() wrapper, because this
 * implementation first is older, and second is much easier to use for this
 * particular case, it should not be removed. Speed over elegance?
 *
 */
public class ObjDateSort extends Obj {
    
    private Obj theObj() {
        return this;
    }
    
    private String dateFmt="yyyy-MM-dd HH:mm:ss,SSS";
    private int pos=0;
    
    
    public ObjDateSort() {
        add(new FunctionSetDateFormat());
        add(new FunctionAsc());
        add(new FunctionGetDateFormat());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    
    public String getTypeName() {
        return "DateSort";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular("DateSort");
    }

    class FunctionSetDateFormat extends Function {
        public String getName() {
            return "setDateFormat";
        }
        public String getShortDesc() {
            return "setDateFormat(str) - set date/time format according to SimpleDateFormat - return self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter: date/time format string");
            if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected one parameter: date/time format string"); 
            dateFmt=((ValueString)params.get(0)).getVal();
            return new ValueObj(theObj());
        }
    }
    
    class FunctionGetDateFormat extends Function {
        public String getName() {
            return "getDateFormat";
        }
        public String getShortDesc() {
            return "getDateFormat() - get current date/time format";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(dateFmt);
        }
    }
    
    class FunctionAsc extends Function {
        public String getName() {
            return "asc";
        }
        public String getShortDesc() {
            return "asc(list) - return sorted list of lines";
        }
        
        class X {
            long time;
            Value v;
            X (long time, Value v) {
                this.time=time;
                this.v=v;
            }
        }
        
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter: list of strings");
            if (!(params.get(0) instanceof ValueList)) throw new Exception("Expected one parameter: list of strings");
            
            List<Value> lines=((ValueList) params.get(0)).getVal();
            SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
            
            List<X> list=new ArrayList<X>();
            
            for (Value line:lines) {
                String s=line.getValAsString();
                if (s.length()<pos+dateFmt.length()) continue;
                long prevTime=0L;
                
                long time;
                try {
                    time=sdf.parse(s.substring(pos, pos+dateFmt.length())).getTime();
                } catch (Exception ex) {
                    time=prevTime;
                }
                list.add(new X(time, line));
                prevTime=time;
            }
            list.sort(new Comparator<X> (){
                public int compare (X a, X b) {
                    if (a.time<b.time) return -1;
                    if (a.time>b.time) return 1;
                    return 0;
                }
            });
            
            List<Value> result=new ArrayList<Value>();
            for (X x:list) {
                result.add(x.v);
            }
            
            return new ValueList(result);
            
        }
    }
    

}
    
