/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

public class ValueObj extends Value {
    
    private Obj val;
    
    public ValueObj (Obj val) {
        this.val=val;
    }
    
    public Obj getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "<obj>";
    }

    @Override
    public String getValAsString() {
        return val.getDescription();
    }
    
    @Override
    public String synthesize() throws Exception {
        return val.synthesize();
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }


    @Override
    public boolean eq(Obj v) {
        if (!(v instanceof ValueObj)) return false;
        ValueObj obj=(ValueObj) v;
        if (obj.getVal()==val) return true;
        return obj.getVal().eq(val);
    }


}
