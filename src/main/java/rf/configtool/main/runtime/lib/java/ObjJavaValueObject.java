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

import rf.configtool.main.runtime.*;

public class ObjJavaValueObject extends ObjJavaValue {
    
    private ObjJavaObject value;
    
    public ObjJavaValueObject (ObjJavaObject value) {
        this.value=value;
    }
    
    @Override
    public Object getAsJavaValue() throws Exception {
        return value.getJavaObject();
    }
    @Override
    public Value getAsCFTValue() throws Exception {
        if (value==null) return new ValueNull();
        return new ValueObj(value);
        
    }


}
