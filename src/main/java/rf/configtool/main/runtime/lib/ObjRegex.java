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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueString;

public class ObjRegex extends Obj {
    
    private String regex;

    private ObjRegex() {
        add(new FunctionMatch());
    }

    public ObjRegex (String regExp) {
        this();
        this.regex=regExp;
    }
    
    
    public boolean matches (String s) {
        return s.matches(regex);
    }

    
    public boolean matchesPartial (String s) {
        return s.matches(".*" + regex + ".*");
    }


    @Override
    public boolean eq(Obj x) {
        if (x instanceof ObjRegex) {
            ObjRegex d=(ObjRegex) x;
            return d.regex.equals(regex);
        }
        return false;
    }

    @Override
    public String synthesize() throws Exception {
        return "Regex(" + (new ValueString(regex)).synthesize() + ")";
    }


    
    @Override
    public String getTypeName() {
        return "Regex";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(regex);
    }
    
    
    class FunctionMatch extends Function {
        public String getName() {
            return "match";
        }
        public String getShortDesc() {
            return "match(str) - returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected 1 parameter");
            String str=getString("str",params,0);
            
            return new ValueBoolean(str.matches(regex));
        }
    }

}
