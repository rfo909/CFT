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

package rf.configtool.main.runtime.lib.cifs;

import jcifs.CIFSContext;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Obj;

public class ObjCIFSContext extends Obj {
    
    private CIFSContext context;

    /**
     * @param context
     * @throws Exception
     */
    public ObjCIFSContext (CIFSContext context) throws Exception {
        this.context=context;
    }

    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "CIFSContext";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("CIFSContext");
    }

    public CIFSContext getContext() {
        return context;
    }
    
}
