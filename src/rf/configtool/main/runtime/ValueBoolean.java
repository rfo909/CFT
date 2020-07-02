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

import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;

public class ValueBoolean extends Value {
    
    private boolean val;
    
    public ValueBoolean (boolean val) {
        this.val=val;
        add (new FunctionNot());
    }
    
    public boolean getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "boolean";
    }

    @Override
    public String getValAsString() {
        return ""+val;
    }
    
    @Override
    public String synthesize() throws Exception {
        return val ? "true" : "false";
    }

    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true;
        return (v instanceof ValueBoolean) && ((ValueBoolean) v).getVal()==val;
    }
    
    @Override
    public boolean getValAsBoolean() {
        return val;
    }


    
    class FunctionNot extends Function {
        public String getName() {
            return "not";
        }
        public String getShortDesc() {
            return "not() - Inverts boolean value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBoolean(!val);
        }

    }
    
    
}
