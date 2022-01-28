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

package rf.configtool.main.runtime.lib;

import rf.configtool.main.runtime.Obj;

/**
 * Common super class of persistent Obj instances
 */
public abstract class ObjPersistent extends Obj {
    
    public abstract String getPersistenceId();
    
    /**
     * Called once for each object with a unique persistence-id
     */
    public void initPersistentObj() {
        // empty
    }
    /**
     * Called every time an expression resolves to an existing persistent object
     */
    public void refreshPersistentObj() {
        // empty
    }
    public void cleanupOnExit() {
        // empty
    }

}
