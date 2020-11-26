package rf.configtool.main.runtime.lib.vgy;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;

import rf.configtool.data.Expr;
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
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.util.net.*;

/**
 * Interface node
 */
public class ObjINode extends Obj {

	private int portForData;
	private int portForAdmin;
	
	private List<HostPort> sNodes=new ArrayList<HostPort>();
	
    public ObjINode () {
    	this.add(new FunctionGetConfiguration());
    	this.add(new FunctionConfigure());
    	this.add(new FunctionStart());
    }

    
    
    private ObjINode self() {
    	return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "INode";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("INode");
    }
    
    class FunctionGetConfiguration extends Function {
        public String getName() {
            return "getConfiguration";
        }
        public String getShortDesc() {
            return "getConfiguration() returns Dict";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            Map<String,Value> data=new HashMap<String,Value>();
            data.put("portForData",  new ValueInt(portForData));
            data.put("portForAdmin",  new ValueInt(portForAdmin));
            
            List<Value> sList=new ArrayList<Value>();
            for (HostPort hp:sNodes) {
            	sList.add(new ValueString(hp.getHostPort()));
            }
            data.put("sNodes", new ValueList(sList));
            
            return new ValueObj(new ObjDict(data));
        }
    }

  
    class FunctionConfigure extends Function {
        public String getName() {
            return "configure";
        }
        public String getShortDesc() {
            return "configure(Dict) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected Dict parameter");
            
            Obj obj=getObj("Dict",params,0);
            if (!(obj instanceof ObjDict)) throw new Exception("Expected Dict parameter");
            
            ObjDict dict=(ObjDict) obj;
            
            portForData=(int) ((ValueInt) dict.getValue("portForData")).getVal();
            portForAdmin=(int) ((ValueInt) dict.getValue("portForAdmin")).getVal();
            
            List<Value> sList=((ValueList) dict.getValue("sNodes")).getVal();
            
            sNodes.clear();
            for (Value v:sList) {
            	String hostPort=((ValueString) v).getVal();
            	sNodes.add(new HostPort(hostPort));
            }

            return new ValueObj(self());
        }
    }

  
    class FunctionStart extends Function {
        public String getName() {
            return "start";
        }
        public String getShortDesc() {
            return "start() - starts service, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            

            
            return new ValueObj(self());
        }
    }

  

}
