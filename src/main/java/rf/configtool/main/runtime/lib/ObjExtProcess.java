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
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

/**
 * Created when running external programs via Dir.runProcess
 */
public class ObjExtProcess extends Obj {
    
    private Process process;
    
    public ObjExtProcess(Process process) {
        this.process=process;
        this.add(new FunctionIsAlive());
        this.add(new FunctionExitCode());
        this.add(new FunctionDestroy());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "ExtProcess";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "ExtProcess";
    }
    
  
    class FunctionIsAlive extends Function {
        public String getName() {
            return "isAlive";
        }
        public String getShortDesc() {
            return "isAlive() - true if running";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueBoolean(process.isAlive());
        }
    }
    
    class FunctionExitCode extends Function {
        public String getName() {
            return "exitCode";
        }
        public String getShortDesc() {
            return "exitCode() - get exit code, unless running, which generates error";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueInt(process.exitValue());
        }
    }

    class FunctionDestroy extends Function {
        public String getName() {
            return "destroy";
        }
        public String getShortDesc() {
            return "destroy() - terminate process";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            process.destroy();
            return new ValueBoolean(true);
        }
    }

}
