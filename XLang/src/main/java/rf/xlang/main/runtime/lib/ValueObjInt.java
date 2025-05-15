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

package rf.xlang.main.runtime.lib;

import java.util.List;

import rf.xlang.main.Ctx;
import rf.xlang.main.runtime.Function;
import rf.xlang.main.runtime.Obj;
import rf.xlang.main.runtime.Value;
import rf.xlang.main.runtime.ValueInt;
import rf.xlang.main.runtime.ValueNull;



/**
 * int value with associated data object, for sorting etc 
 */
public class ValueObjInt extends ValueInt {

    private Value data;
    
    public ValueObjInt(long value, Value data) {
        super(value);
        if (data==null) data=new ValueNull();
        this.data=data;
        
        add(new FunctionData());
    }
    
    private Obj self() {
        return this;
    }
    
    // DO NOT override eq() - need to just compare the value, not the extra data

    public String getTypeName() {
        return "Int";
    }

    class FunctionData extends Function {
        public String getName() {
            return "data";
        }
        public String getShortDesc() {
            return "data() - get data value or null if not defined";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return data;
        }
    }

}
