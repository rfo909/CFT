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

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.PropsFile;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

public class ObjSys extends Obj {
    
    public ObjSys() {
        add(new FunctionVersion());
        add(new FunctionFunctions());
        add(new FunctionLog());
        add(new FunctionCodeDirs());

    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
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
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionVersion extends Function {
        public String getName() {
            return "version";
        }
        public String getShortDesc() {
            return "version() - returns CFT version string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
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
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> values=new ArrayList<Value>();
            List<String> names = ctx.getObjGlobal().getCodeHistory().getNames();
            for (String name:names) values.add(new ValueString(name));
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
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter msg");
            String line=getString("msg", params, 0);
            ctx.getOutText().addSystemMessage(line);
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
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> list=new ArrayList<Value>();
            for (String s:ctx.getObjGlobal().getPropsFile().getCodeDirs()) {
            	ObjDir dir=new ObjDir(s);
            	list.add(new ValueObj(dir));
            }
            return new ValueList(list);
        }
    }
    


}
