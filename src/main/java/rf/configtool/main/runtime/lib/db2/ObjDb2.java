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

package rf.configtool.main.runtime.lib.db2;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.CtxCloseHook;
import rf.configtool.main.SoftErrorException;
import rf.configtool.main.OutText;
import rf.configtool.main.PropsFile;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.parsetree.Expr;
import rf.configtool.util.Hash;

public class ObjDb2 extends Obj {

    private Db2 db2;
    
    public ObjDb2 () {
        this.db2=Db2.getInstance();
        
        this.add(new FunctionSet());
        this.add(new FunctionGet());
        this.add(new FunctionKeys());
        this.add(new FunctionCollections());
        this.add(new FunctionDeleteCollection());
        this.add(new FunctionObtainLock());
        this.add(new FunctionReleaseLock());
        this.add(new FunctionGetLockFile());
    }
    
    private ObjDb2 self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "Db2";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Db2");
    }
    
    class FunctionSet extends Function {
        public String getName() {
            return "set";
        }
        public String getShortDesc() {
            return "set(key,value) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 3) throw new Exception("Expected string parameters collection, key, value");
            String collection=getString("collection",params,0);
            String key=getString("key",params,1);
            String value=getString("value",params,2);
            db2.set(collection,key,value);
            
            return new ValueObj(self());
        }
    }

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get(key) - returns string or null";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected string parameters collection, key");
            String collection=getString("collection", params, 0);
            String key=getString("key",params,1);
            String value = db2.get(collection,key);
            if (value==null) {
                return new ValueNull();
            } else {
                return new ValueString(value);
            }
        }
    }

    class FunctionKeys extends Function {
        public String getName() {
            return "keys";
        }
        public String getShortDesc() {
            return "keys(collection) - return list of keys";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter collection");
            String collection=getString("collection",params,0);
            List<String> keys=db2.getKeys(collection);
            List<Value> list=new ArrayList<Value>();
            for (String key:keys) {
                list.add(new ValueString(key));
            }
            return new ValueList(list);
        }
    }

    class FunctionCollections extends Function {
        public String getName() {
            return "collections";
        }
        public String getShortDesc() {
            return "collections() - return list of collection names";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<String> collections=db2.getCollections();
            List<Value> list=new ArrayList<Value>();
            for (String c:collections) {
                list.add(new ValueString(c));
            }
            return new ValueList(list);
        }
    }
    
    class FunctionDeleteCollection extends Function {
        public String getName() {
            return "deleteCollection";
        }
        public String getShortDesc() {
            return "keys() - return list of keys";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter collection");
            String collection=getString("collection",params,0);
            db2.deleteCollection(collection);
            return new ValueObj(self());
        }
    }

    private File getLockFile (String name) throws Exception {
        String lockFileDir=(File.separator.equals("/") ? "/tmp" : "c:\\temp");
        Hash hash=new Hash();
        hash.add(name.getBytes("UTF-8"));
        return new File(lockFileDir + File.separator + hash.getHashString() + ".lock");
    }
    
    class FunctionObtainLock extends Function {
        public String getName() {
            return "obtainLock";
        }
        public String getShortDesc() {
            return "obtainLock(name, timeoutmillis) - throws error if failing";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected parameters: name, timeoutmillis");
            
            // ensure we have a valid file name, hashing the name
            String name=getString("name", params, 0);
            int timeout=(int) getInt("timeoutmillis", params, 1);
            
            File file=getLockFile(name);
            LockFile.obtainLock(file, timeout);
            return new ValueBoolean(true);
        }
    }


    class FunctionReleaseLock extends Function {
        public String getName() {
            return "releaseLock";
        }
        public String getShortDesc() {
            return "releaseLock(name) - release lock obtained previously";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected name parameter");
            String name=getString("name",params,0);
            
            File file=getLockFile(name);
            LockFile.freeLock(file);
            
            return new ValueBoolean(true);
        }
    }


    class FunctionGetLockFile extends Function {
        public String getName() {
            return "getLockFile";
        }
        public String getShortDesc() {
            return "getLockFile(name) - return corresponding lock file for named lock";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected name parameter");
            String name=getString("name",params,0);
            
            File file=getLockFile(name);
            return new ValueObj(new ObjFile(file.getAbsolutePath(), Protection.NoProtection));
        }
    }


}
