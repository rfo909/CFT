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
		add(new FunctionNames());
		add(new FunctionGet());
		add(new FunctionDelete());
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
	 * 		add(new FunctionAdd());
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
    
    
    class FunctionNames extends Function {
        public String getName() {
            return "names";
        }

        public String getShortDesc() {
            return "names() - returns list of process names";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no parameters");
        	List<String> names=ctx.getObjGlobal().getRoot().getBackgroundProcesses().getNames();
        	List<Value> values=new ArrayList<Value>();
        	for (String n:names) {
        		values.add(new ValueString(n));
        	}
        	return new ValueList(values);
        }

    }        

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }

        public String getShortDesc() {
            return "get(name) - returns list of processes for name";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected parameter name");
        	String name=getString("name",params,0);
        	List<ObjProcess> processes=ctx.getObjGlobal().getRoot().getBackgroundProcesses().getProcesses(name);
        	List<Value> values=new ArrayList<Value>();
        	for (ObjProcess p:processes) values.add(new ValueObj(p));
        	return new ValueList(values);
        }

    }
    
    
    class FunctionDelete extends Function {
        public String getName() {
            return "delete";
        }

        public String getShortDesc() {
            return "delete(name) - delete processes by name - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected name parameter");
        	String name = getString("name",params,0);
        	ctx.getObjGlobal().getRoot().getBackgroundProcesses().deleteProcesses(name);
        	return new ValueObj(self());
        }

    }        
    
    
}
