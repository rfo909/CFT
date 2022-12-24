package rf.configtool.main.runtime.lib.web;

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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

/**
 * Web server stuff
 */

public class ObjWeb extends Obj {
    
    public ObjWeb() {
        this.add(new FunctionServer());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Web";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Web";
    }
    
    private Obj theObj () {
        return this;
    }
    
    class FunctionServer extends Function {
        public String getName() {
            return "Server";
        }
        public String getShortDesc() {
            return "Server(port) - returns WebServer object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected port parameter (int)");
            int port=(int) getInt("port", params, 0);
            return ctx.getObjGlobal().getOrAddPersistentObject(new ObjWebServer(port,ctx));
        }
    }
    
    

}
