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

package rf.configtool.main.runtime.lib.java;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueNull;

public class ObjJavaValueInt extends ObjJavaValue {
    
    private Integer value;
    
    public ObjJavaValueInt (Integer value) {
        this.value=value;
    }
    
    @Override
    public Object getAsJavaValue() throws Exception {
        return value;
    }
    @Override
    public Value getAsCFTValue() throws Exception {
        if (value==null) return new ValueNull();
        return new ValueInt(value);
    }


}
