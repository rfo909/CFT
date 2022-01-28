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

package rf.configtool.main.runtime.lib.db;

import java.io.File;
import java.util.List;
import java.util.UUID;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.root.LockManager;
import rf.configtool.util.Hash;

public class ObjDb extends Obj {

    public ObjDb () {     
        this.add(new FunctionDb2());
        this.add(new FunctionUUID());
        this.add(new FunctionObtainLock());
        this.add(new FunctionReleaseLock());

    }
    
    private ObjDb self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "Db";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Db");
    }
   
    class FunctionDb2 extends Function {
        public String getName() {
            return "Db2";
        }
        public String getShortDesc() {
            return "Db2() - create Db object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjDb2());
        }
    } 
    
    
    class FunctionUUID extends Function {
        public String getName() {
            return "UUID";
        }
        public String getShortDesc() {
            return "UUID() - generate random UUID string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(UUID.randomUUID().toString());
        }
    } 
 
    
    class FunctionObtainLock extends Function {
        public String getName() {
            return "obtainLock";
        }
        public String getShortDesc() {
            return "obtainLock(name, timeoutmillis) - obtain named lock - throws error if failing";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected parameters: name, timeoutmillis");
            
            // ensure we have a valid file name, hashing the name
            String name=getString("name", params, 0);
            int timeout=(int) getInt("timeoutmillis", params, 1);
            
            LockManager.obtainLock(name, timeout);
            return new ValueBoolean(true);
        }
    }


    class FunctionReleaseLock extends Function {
        public String getName() {
            return "releaseLock";
        }
        public String getShortDesc() {
            return "releaseLock(name) - release named lock obtained previously - throws error if not owner";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected name parameter");
            String name=getString("name",params,0);
            
            LockManager.freeLock(name);
            
            return new ValueBoolean(true);
        }
    }


 
    
   
 
}
