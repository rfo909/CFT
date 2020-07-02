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

package rf.configtool.main;

import java.util.*;

/**
 * Whenever calling a script by "script:func" we can supply a list of named
 * values, via a Dict, that replaces corresponding named functions. This is
 * only available for values that can be synthesized.
 */
public class FuncOverrides {
    private Map<String,String> map;

    public FuncOverrides (Map<String,String> map) {
        this.map=map;
    }
    
    public String getFuncOverride (String name) {
        return map.get(name);
    }
}
