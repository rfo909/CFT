package rf.configtool.main.runtime.lib.net;

import java.io.PrintStream;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjPersistent;
import rf.configtool.main.runtime.lib.net.low.HostInfo;
import rf.configtool.main.runtime.reporttool.Report;

/**
 * In the CFT spirit, networking in CFT is advanced behind the scenes, presenting
 * an easy-to-use functional interface.
 */
public class ObjNet extends ObjPersistent {

	private HostInfo thisHost;
	private List<ListenPort> listenPorts=new ArrayList<ListenPort>();
	private List<Connection> connections=new ArrayList<Connection>();
	private SessionManager mgr;
	
	public ObjNet() {
    	add(new FunctionListen());
    	add(new FunctionConnect());
    	add(new FunctionStatus());
    	add(new FunctionPublicName());
    	this.thisHost=new HostInfo();
    	mgr=new SessionManager(this.thisHost);
    }
    
    private void addListenPort (Ctx ctx, int port) throws Exception {
    	Stdio stdio=ctx.getStdio();
    	
    	for (ListenPort p:listenPorts) {
    		if (p.getPort()==port) {
    			throw new Exception("Can not add listen port " + port + " - already in use");
    		}
    	}

    	
    	ListenPort lp=new ListenPort(stdio, mgr, port);
    	listenPorts.add(lp);
    	stdio.println("% Access code is " + lp.getAccessCode());
    }
    
    private void addConnection (Ctx ctx, String host, int port, String accessCode) throws Exception {
    	Stdio stdio=ctx.getStdio();

//    	for (Connection c:connections) {
//    		if (c.getHost().equals(host)) {
//    			throw new Exception("Can not add connection to host " + host + " - already exists, on port " + c.getPort());
//    		}
//    	}
    	connections.add(new Connection(stdio, mgr, host, port, accessCode));
    	stdio.println("% Connection added");
    }

    @Override 
    public String getPersistenceId() {
        return "ObjNet Singleton";
    }
 
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }

    @Override
    public void cleanupOnExit() {
    	mgr.cleanupOnExit();
    }


    public String getTypeName() {
        return "Net";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Net";
    }
    
    private Obj theObj () {
        return this;
    }
    
    private void verifyPublicName (Stdio stdio) throws Exception {
    	while (!thisHost.nameOk()) {
    		stdio.print("Enter public name for this host: ");
    		String s=stdio.getInputLine().trim();
    		thisHost.updateName(s);
    	}
    }
 

    
    class FunctionListen extends Function {
        public String getName() {
            return "listen";
        }
        public String getShortDesc() {
            return "listen(port) - create listen-server on port";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected tcp port");
            int port=(int) getInt("port", params, 0);
            
            verifyPublicName(ctx.getStdio());
            
            addListenPort(ctx, port);
            
            return new ValueBoolean(true);
        }
    }
    
    class FunctionConnect extends Function {
        public String getName() {
            return "connect";
        }
        public String getShortDesc() {
            return "connect(host,port,accessCode) - create connection to remote CFT host";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 3) throw new Exception("Expected parameters host, port, accessCode");
            String host=getString("host", params, 0);
            int port=(int) getInt("port", params, 1);
            String accessCode=getString("accessCode", params, 2).trim();
            
            verifyPublicName(ctx.getStdio());
            
            addConnection(ctx, host, port, accessCode);
            
            
            return new ValueBoolean(true);
        }
    }
    

    class FunctionStatus extends Function {
        public String getName() {
            return "status";
        }
        public String getShortDesc() {
            return "status() - show network status";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");

            OutText out = ctx.getOutText();
            if (listenPorts.size() > 0) {
	            for (ListenPort p:listenPorts) {
	            	int port=p.getPort();
	            	out.addReportData("server", ""+port, p.getAccessCode());
	            }
            }
//            if (connections.size() > 0) {
//	            for (Connection c:connections) {
//	            	out.addReportData("connection-to", c.getHost(), ""+c.getPort());
//	            }
//            }
            
            mgr.report(out);
            
            Report rep=new Report();
            List<String> result=rep.formatDataValues(out.getData());
            
            
            List<Value> x=new ArrayList<Value>();
            for (String s:result) x.add(new ValueString(s));
            return new ValueList(x);
        }
    }
    

    class FunctionPublicName extends Function {
        public String getName() {
            return "publicName";
        }
        public String getShortDesc() {
            return "publicName(name) - set host public name";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected public name string parameter");
            String publicName=getString("name", params, 0);
            thisHost.updateName(publicName);
            return new ValueString(publicName);
        }
    }
    


}
