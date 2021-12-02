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

package rf.configtool.main.runtime.lib.text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.Regex;

public class ObjFilter extends Obj {
    
    private List<String> filters;
    private boolean caseSensitive=true;
    
    public ObjFilter() {
        this.filters=new ArrayList<String>();
        this.add(new FunctionStartWith());
        this.add(new FunctionContains());
        this.add(new FunctionContainsRe());
        this.add(new FunctionEndWith());
        this.add(new FunctionCase());
        this.add(new FunctionMatch());
    }
    
    private Obj self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Filter";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String get(List<String> value) {
        StringBuffer sb=new StringBuffer();
        boolean sep=false;
        for (String s:value) {
            if (sep) sb.append(" && ");
            sb.append(s);
            sep=true;
        }
        return sb.toString();
    }
    

    private String getDesc() {
        return get(filters); 
    }
    

    class FunctionStartWith extends Function {
        public String getName() {
            return "startsWith";
        }
        public String getShortDesc() {
            return "startsWith(str) - add search criterium (AND)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueString)) throw new Exception("Expected string parameter");

            String str=((ValueString) value).getVal();
            filters.add("^"+Regex.createRegex(str,caseSensitive)+".*$");
            
            return new ValueObj(self());
        }
    }

    class FunctionContains extends Function {
        public String getName() {
            return "contains";
        }
        public String getShortDesc() {
            return "contains(str) - add search criterium (AND)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueString)) throw new Exception("Expected string parameter");

            String str=((ValueString) value).getVal();
            filters.add("^.*" + Regex.createRegex(str, caseSensitive) + ".*$");
            return new ValueObj(self());
        }
    }
    
    class FunctionContainsRe extends Function {
        public String getName() {
            return "containsRe";
        }
        public String getShortDesc() {
            return "containsRe(str) - add regular expression search criterium (AND)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueString)) throw new Exception("Expected string parameter");

            String str=((ValueString) value).getVal();
            filters.add(str);
            return new ValueObj(self());
        }
    }

    class FunctionEndWith extends Function {
        public String getName() {
            return "endsWith";
        }
        public String getShortDesc() {
            return "endsWith(str) - add search criterium (AND)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueString)) throw new Exception("Expected string parameter");
            
            String str=((ValueString) value).getVal();
            filters.add("^.*" + Regex.createRegex(str, caseSensitive) + "$");
            return new ValueObj(self());
        }
    }

    class FunctionCase extends Function {
        public String getName() {
            return "case";
        }
        public String getShortDesc() {
            return "case(boolean) - modify meaning of additional search criteria";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected boolean parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueBoolean)) throw new Exception("Expected boolean parameter");

            boolean b=((ValueBoolean)value).getVal();
            caseSensitive=b;
            
            return new ValueObj(self());
        }
    }


    class FunctionMatch extends Function {
        public String getName() {
            return "match";
        }
        public String getShortDesc() {
            return "match(str) - returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueString)) throw new Exception("Expected string parameter");

            String s=((ValueString) value).getVal();
            
            boolean b=matches(s);
            
            return new ValueBoolean(b);
        }
    }

    public boolean matches (String s) {
        for (String regex:filters) {
            if (!s.matches(regex)) return false;
        }
        return true;
    }

}
