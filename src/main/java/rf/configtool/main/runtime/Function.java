/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

/**
 * Superclass of all functions inside Obj (java objects of the language)
 * 
 */
import java.util.List;

import rf.configtool.main.Ctx;

public abstract class Function {
    
    public abstract String getName();
    public abstract String getShortDesc();
    
    public abstract Value callFunction (Ctx ctx, List<Value> params) throws Exception;
    
}
