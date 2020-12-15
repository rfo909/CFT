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

public class ObjSNode extends Obj {

	private int portForData = 31033;
	private int portForAdmin = 31034;
	private String dataDir=".";
	private String id;
	
    public ObjSNode () {
    	this.id=UUID.randomUUID().toString();
    	this.add(new FunctionGetConfiguration());
    	this.add(new FunctionConfigure());
    	this.add(new FunctionStart());
    }

    
    
    private ObjSNode self() {
    	return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "SNode";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("SNode");
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
            data.put("dataDir",new ValueString(dataDir));
            data.put("id", new ValueString(id));
            
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
            dataDir=((ValueString) dict.getValue("dataDir")).getVal();
            id=((ValueString) dict.getValue("id")).getVal();
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
            
            TCPServer tcpServerData=new TCPServer(portForData);
            TCPServer tcpServerAdmin=new TCPServer(portForAdmin);
            
            DataProcess data = new DataProcess(tcpServerData);
            AdminProcess adm = new AdminProcess(tcpServerAdmin, data);
            
            (new Thread(data)).start();
            (new Thread(adm)).start();
            
            return new ValueObj(self());
        }
    }

  
    class DataProcess implements Runnable {
    	private TCPServer tcpServer;
    	private boolean terminate;
    	private List<String> logLines=new ArrayList<String>();
    	
    	public DataProcess (TCPServer tcpServer) {
    		this.tcpServer=tcpServer;
    	}
    	public synchronized void doTerminate() {
    		terminate=true;
    	}
    	private synchronized void log (String s) {
    		logLines.add(s);
    		while(logLines.size() > 100) logLines.remove(0);
    	}
    	public synchronized List<String> getLogLines() {
    		List<String> x = logLines;
    		logLines=new ArrayList<String>();
    		return x;
    	}
    	public void run () {
    		while (!terminate) {
    			TCPServerConnection conn = tcpServer.getConnection();
    				// NOTE: caller has to close connection!
    			if (conn==null) {
    				try {Thread.sleep(10);} catch (Exception ex) {}
    				continue;
    			}
    			IO io=conn.getIO();
    			// process connection, then close it
    			try {
    				String command = io.readInputString();
    				if (command.equals("SAVE")) {
    					String key = io.readInputString();
    					String value = io.readInputString();
    					log("SAVE key="+key+" value=" + value);
    					io.writeOutputString("OK");
    				} else if (command.equals("GET")) {
    					String key = io.readInputString();
    					io.writeOutputString("0");
    					log("GET key=" + key);
    				} else {
    					io.writeOutputString("Invalid command: " + command);
    				}
    			} catch (Exception ex) {
    				// ignore
    			} 
    		}
    	}
    }

    class AdminProcess implements Runnable {
    	private TCPServer tcpServer;
    	private DataProcess dataProcess;
		boolean terminate=false;

		public AdminProcess (TCPServer tcpServer, DataProcess dataProcess) {
    		this.tcpServer=tcpServer;
    		this.dataProcess=dataProcess;
    	}
    	public void run () {
    		while (!terminate) {
    			TCPServerConnection conn = tcpServer.getConnection();
    				// NOTE: caller has to close connection!
    			if (conn==null) {
    				try {Thread.sleep(10);} catch (Exception ex) {};
    				continue;
    			} 
    			// process connection, then close it
    			IO io=conn.getIO();
    			try {
    				String command = io.readInputString();
    				if (command.equals("QUIT")) {
    					terminate=true;
    					io.writeOutputString("Shutting down");
    				} else if (command.equals("GETLOG")) {
    					List<String> logLines=dataProcess.getLogLines();
    					io.writeOutputString(""+logLines.size());
    					for (String s:logLines) {
    						io.writeOutputString(s);
    					}
    				} else {
    					io.writeOutputString("Invalid command: " + command);
    				}
    			} catch (Exception ex) {
    				// ignore
    				ex.printStackTrace();
    			} 
    		}
    		dataProcess.doTerminate();
    	}
    }

}
