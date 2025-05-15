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

package rf.xlang.main.runtime.lib;

import java.io.File;
import java.util.List;
import java.util.UUID;

import rf.xlang.main.Ctx;
import rf.xlang.main.Version;
import rf.xlang.main.runtime.Function;
import rf.xlang.main.runtime.Obj;
import rf.xlang.main.runtime.Value;
import rf.xlang.main.runtime.ValueBoolean;
import rf.xlang.main.runtime.ValueInt;
import rf.xlang.main.runtime.ValueObj;
import rf.xlang.main.runtime.ValueString;
import rf.xlang.main.runtime.lib.app.ObjApp;

public class ObjSys extends Obj {

    public ObjSys() {
        Function[] arr={
                new FunctionVersion(),
                new FunctionSleep(),
                new FunctionIsWindows(),
                new FunctionHomeDir(),
                new FunctionUchar(),
                new FunctionFileSeparator(),
                new FunctionCPUCores(),
                new FunctionGetType(),
                new FunctionCurrentTimeMillis(),
                new FunctionUUID(),
                new FunctionEnvironment(),


        };
        setFunctions(arr);
        
    }

    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "Sys";
    }

    private String getDesc() {
        return "Sys";
    }

    private Obj theObj() {
        return this;
    }

    class FunctionVersion extends Function {
        public String getName() {
            return "version";
        }

        public String getShortDesc() {
            return "version() - returns CFT version string";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            return new ValueString(Version.getVersion());
        }
    }

    class FunctionSleep extends Function {
        public String getName() {
            return "sleep";
        }

        public String getShortDesc() {
            return "sleep(millis) - returns current time in ms";
        }

        @Override
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1)
                throw new Exception("Expected millis parameter");
            int millis = (int) getInt("millis", params, 0);
            try {
                Thread.sleep(millis);
            } catch (Exception ex) {
                // ignore
            }
            return new ValueInt(System.currentTimeMillis());
        }
    }

    class FunctionIsWindows extends Function {
        public String getName() {
            return "isWindows";
        }

        public String getShortDesc() {
            return "isWindows() - returns boolean";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            return new ValueBoolean(ctx.getObjGlobal().runningOnWindows());
        }
    }



    class FunctionHomeDir extends Function {
        public String getName() {
            return "homeDir";
        }

        public String getShortDesc() {
            return "homeDir() - returns Dir object for CFT start dir";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            File f=new File(".");
            String path=f.getCanonicalPath();
            ObjDir x=new ObjDir(path);
            return new ValueObj(x);
        }
    }
    
    

    class FunctionUchar extends Function {
        public String getName() {
            return "uchar";
        }

        public String getShortDesc() {
            return "uchar(str|int) - create unicode char from \\uXXXX string notation (the \\u part is optional) - or int value";
        }
        
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1)
                throw new Exception("Expected str|int parameter");
            if (params.get(0) instanceof ValueInt) {
                char c=(char) getInt("int",params,0);
                return new ValueString(""+c);
            }
            // String parameter
            String str=getString("str",params,0);
            if (str.toLowerCase().startsWith("\\u")) str=str.substring(2);
            char c=(char) (Integer.parseInt(str,16));
            return new ValueString(""+c);
        }
    }


    class FunctionFileSeparator extends Function {
        public String getName() {
            return "fileSeparator";
        }

        public String getShortDesc() {
            return "fileSeparator() - returns file separator string";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
           
            return new ValueString(File.separator);
        }

    }           

    
    class FunctionCPUCores extends Function {
        public String getName() {
            return "cpuCores";
        }

        public String getShortDesc() {
            return "cpuCores() - get CPU core count";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            int count=Runtime.getRuntime().availableProcessors();
            return new ValueInt(count);
        }

    }         



   class FunctionGetType extends Function {
        public String getName() {
            return "getType";
        }
        public String getShortDesc() {
            return "getType(any) - get value or object type";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter any");
            Value v=params.get(0);
            String s;
            if (v instanceof ValueObj) {
                s=((ValueObj) v).getVal().getTypeName();
            } else {
                s=v.getTypeName();
            }
            return new ValueString(s);
        }
    }
   

    class FunctionCurrentTimeMillis extends Function {
        public String getName() {
            return "currentTimeMillis";
        }
        public String getShortDesc() {
            return "currentTimeMillis() - return current time as millis";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(System.currentTimeMillis());
        }
    }

    class FunctionUUID extends Function {
        public String getName() {
            return "UUID";
        }
        public String getShortDesc() {
            return "UUID() - return uuid string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            String s = UUID.randomUUID().toString();
            return new ValueString(s);
        }
    }

    class FunctionApp extends Function {
        public String getName() {
            return "App";
        }
        public String getShortDesc() {
            return "App() - returns App object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjApp(ctx.getObjGlobal()));
        }
    }


}
