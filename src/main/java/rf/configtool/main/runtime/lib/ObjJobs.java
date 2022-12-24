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

package rf.configtool.main.runtime.lib;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

/**
 * A specialized "session store" for Processes running in the background, as ObjProcess can not be 
 * synthesized. 
 */
public class ObjJobs extends Obj {

    public ObjJobs() {
        add(new FunctionAdd());
        add(new FunctionNamesRunning());
        add(new FunctionNamesCompleted());
        add(new FunctionGetCompleted());
        add(new FunctionGetRunning());
        add(new FunctionDeleteCompleted());
    }

    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "Jobs";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "Jobs";
    }

    private Obj self() {
        return this;
    }

    
    /*
     *      add(new FunctionAdd());
        add(new FunctionNames());
        add(new FunctionGet());
        add(new FunctionDelete());

     */

    class FunctionAdd extends Function {
        public String getName() {
            return "add";
        }

        public String getShortDesc() {
            return "add(Process,name) - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected parameters Process, name");
            Obj proc1=getObj("Process",params,0);
            String name=getString("name",params,1);
            
            if (proc1 instanceof ObjProcess) {
                ObjProcess proc=(ObjProcess) proc1;
                ctx.getObjGlobal().getRoot().getBackgroundProcesses().add(name, proc);
                return new ValueObj(self());
            } else {
                throw new Exception("Expected parameters Process, name");
            }
        }

    }
    
    
    class FunctionNamesRunning extends Function {
        public String getName() {
            return "namesRunning";
        }

        public String getShortDesc() {
            return "namesRunning() - returns list of running process names";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<String> names=ctx.getObjGlobal().getRoot().getBackgroundProcesses().getNamesRunning();
            List<Value> values=new ArrayList<Value>();
            for (String n:names) {
                values.add(new ValueString(n));
            }
            return new ValueList(values);
        }

    }        

    class FunctionNamesCompleted extends Function {
        public String getName() {
            return "namesCompleted";
        }

        public String getShortDesc() {
            return "namesCompleted() - returns list of completed process names";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<String> names=ctx.getObjGlobal().getRoot().getBackgroundProcesses().getNamesCompleted();
            List<Value> values=new ArrayList<Value>();
            for (String n:names) {
                values.add(new ValueString(n));
            }
            return new ValueList(values);
        }

    }        

    class FunctionGetCompleted extends Function {
        public String getName() {
            return "getCompleted";
        }

        public String getShortDesc() {
            return "getCompleted(name) - returns first completed process by name";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter name");
            String name=getString("name",params,0);
            ObjProcess proc=ctx.getObjGlobal().getRoot().getBackgroundProcesses().getCompletedProcess(name);
            return new ValueObj(proc);
        }

    }
    
    
    class FunctionGetRunning extends Function {
        public String getName() {
            return "getRunning";
        }

        public String getShortDesc() {
            return "getRunning(name) - returns first running process by name";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter name");
            String name=getString("name",params,0);
            ObjProcess proc=ctx.getObjGlobal().getRoot().getBackgroundProcesses().getRunningProcess(name);
            return new ValueObj(proc);
        }

    }
  
    
    class FunctionDeleteCompleted extends Function {
        public String getName() {
            return "deleteCompleted";
        }

        public String getShortDesc() {
            return "deleteCompleted(name) - delete all completed processes by name - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected name parameter");
            String name = getString("name",params,0);
            ctx.getObjGlobal().getRoot().getBackgroundProcesses().deleteCompletedProcesses(name);
            return new ValueObj(self());
        }

    }        
    
    
}
