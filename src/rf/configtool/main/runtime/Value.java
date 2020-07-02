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

import java.util.ArrayList;
import java.util.List;

public abstract class Value extends Obj {
    
    public abstract String getTypeName();
    
    public ColList getContentDescription() {
        return ColList.list().regular(getValAsString());
    }
    public abstract String getValAsString();
    
    public abstract boolean eq(Obj v);
    
    /**
     * boolean false, null and empty list are false, all other values are true
     */
    public abstract boolean getValAsBoolean();

    
}
