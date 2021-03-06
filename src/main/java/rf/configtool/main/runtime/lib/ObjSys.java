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

package rf.configtool.main.runtime.lib;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;

import rf.configtool.data.Expr;
import rf.configtool.main.Ctx;
import rf.configtool.main.CtxCloseHook;
import rf.configtool.main.SoftErrorException;
import rf.configtool.main.Stdio;
import rf.configtool.main.StdioReal;
import rf.configtool.main.OutText;
import rf.configtool.main.PropsFile;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

public class ObjSys extends Obj {

    public ObjSys() {
		Function[] arr={
				new FunctionVersion(),
				new FunctionFunctions(),
				new FunctionLog(),
				new FunctionCodeDirs(),
				new FunctionOutCount(),
				new FunctionLastResult(),
				new FunctionSleep(),
				new FunctionStdin(),
				new FunctionIsWindows(),
				new FunctionSavefile(),
				new FunctionScriptName(),
				new FunctionUptime(),
				new FunctionHomeDir(),
				new FunctionUchar(),
				new FunctionSessionUUID(),
				new FunctionReadPassword(),
				new FunctionSecureSessionID(),
				new FunctionClone(),
				new FunctionFileSeparator()
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

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
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
            return new ValueString(new Version().getVersion());
        }
    }

    class FunctionFunctions extends Function {
        public String getName() {
            return "functions";
        }

        public String getShortDesc() {
            return "functions() - returns list of custom functions";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            List<Value> values = new ArrayList<Value>();
            List<String> names = ctx.getObjGlobal().getCodeHistory().getNames();
            for (String name : names)
                values.add(new ValueString(name));
            return new ValueList(values);
        }
    }

    class FunctionLog extends Function {
        public String getName() {
            return "log";
        }

        public String getShortDesc() {
            return "log(msg) - add message to system messages, which are presented after run";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1)
                throw new Exception("Expected string parameter msg");
            String line = getString("msg", params, 0);
            ctx.getObjGlobal().addSystemMessage(line);
            return new ValueBoolean(true);
        }
    }

    class FunctionCodeDirs extends Function {
        public String getName() {
            return "codeDirs";
        }

        public String getShortDesc() {
            return "codeDirs() - returns list of code dirs (see " + PropsFile.PROPS_FILE + ")";
        }

        @Override
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            List<Value> list = new ArrayList<Value>();
            for (String s : ctx.getObjGlobal().getRoot().getPropsFile().getCodeDirs()) {
                ObjDir dir = new ObjDir(s, Protection.NoProtection);
                list.add(new ValueObj(dir));
            }
            return new ValueList(list);
        }
    }

    class FunctionOutCount extends Function {
        public String getName() {
            return "outCount";
        }

        public String getShortDesc() {
            return "outCount() - returns number of values emitted via out() / report()";
        }

        @Override
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            int out = ctx.getOutData().getOutDataLength();
            int report = ctx.getOutText().getData().size();
            return new ValueInt(out + report);
        }
    }

    class FunctionLastResult extends Function {
        public String getName() {
            return "lastResult";
        }

        public String getShortDesc() {
            return "lastResult() - returns last interactive result";
        }

        @Override
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            return ctx.getObjGlobal().getRoot().getLastResult();
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

    class FunctionStdin extends Function {
        public String getName() {
            return "stdin";
        }

        public String getShortDesc() {
            return "stdin(list|...) - buffer stdin lines - no params to clear - returns number of lines cached";
        }

        @Override
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                // clear buffered input
                ctx.getStdio().clearBufferedInputLines();
            } else {

                List<Value> values;
                if (params.size() == 1 && (params.get(0) instanceof ValueList)) {
                    values = ((ValueList) params.get(0)).getVal();
                } else {
                    values = params;
                }

                for (Value v : values) {
                    String s = v.getValAsString();
                    ctx.getStdio().addBufferedInputLine(s);
                }
            }
            return new ValueInt(ctx.getStdio().getCachedInputLineCount());
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

    class FunctionSavefile extends Function {
        public String getName() {
            return "savefile";
        }
        public String getShortDesc() {
            return "savefile() - returns File object for savefile or null if no savefile";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            try {
                File f=ctx.getObjGlobal().getSavefile();
                return new ValueObj(new ObjFile(f.getCanonicalPath(), Protection.NoProtection));
            } catch (Exception ex) {
                return new ValueNull();
            }
        }
    } 


    class FunctionScriptName extends Function {
        public String getName() {
            return "scriptName";
        }

        public String getShortDesc() {
            return "scriptName() - returns script name or null if not saved yet";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            String scriptName=ctx.getObjGlobal().getScriptName();
            if (scriptName==null) return new ValueNull();
            return new ValueString(scriptName);
        }
    }
    
    
    class FunctionUptime extends Function {
        public String getName() {
            return "uptime";
        }

        public String getShortDesc() {
            return "uptime() - returns a Date.Duration object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            long uptime=System.currentTimeMillis() - ctx.getObjGlobal().getRoot().getStartTime();
            ObjDuration d=new ObjDuration(uptime);
            return new ValueObj(d);
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
            ObjDir x=new ObjDir(path, Protection.NoProtection);
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

    class FunctionSessionUUID extends Function {
        public String getName() {
            return "sessionUUID";
        }

        public String getShortDesc() {
            return "sessionUUID() - return per CFT-session UUID - for storing session values in Db etc";
        }
        
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(ctx.getObjGlobal().getRoot().getSessionUUID());
        }
    }
    
    
    class FunctionReadPassword extends Function {
        public String getName() {
            return "readPassword";
        }

        public String getShortDesc() {
            return "readPassword(prompt?) - returns password or error if stdio != StdioReal";
        }
        
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            String prompt="";
            if (params.size() == 1) {
                prompt=getString("prompt",params,0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional parameter prompt");
            }
            Stdio stdio = ctx.getStdio();
            if (stdio instanceof StdioReal) {
                StdioReal real=(StdioReal) stdio;
                real.print(prompt);
                return new ValueString(real.readPassword());
            } else {
                throw new Exception("readPassword() requires StdioReal"); 
            }
        }
    }
    
    
    class FunctionSecureSessionID extends Function {
        public String getName() {
            return "secureSessionId";
        }

        public String getShortDesc() {
            return "secureSessionId() - return secure session id as secure Binary object";
        }
        
        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBinary(ctx.getObjGlobal().getRoot().getSecureSessionID(), true);
        }
    }
    
    class FunctionClone extends Function {
        public String getName() {
            return "clone";
        }

        public String getShortDesc() {
            return "clone(value) - creates clone via eval(syn()) - error if value not synthesizable";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1)
                throw new Exception("Expected value parameter");
            Value v = params.get(0);
            return v.createClone(ctx);
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
    

}
