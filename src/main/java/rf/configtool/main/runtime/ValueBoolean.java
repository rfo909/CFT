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

package rf.configtool.main.runtime;

public class ValueBoolean extends Value implements IsSynthesizable {
    
    private boolean val;
    
    public ValueBoolean (boolean val) {
        this.val=val;
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
    public String createCode() throws Exception {
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
    
    
}
