package rf.configtool.main.runtime.lib.web;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjClosure;
import rf.configtool.main.runtime.lib.ObjPersistent;

/**
 * Web server stuff
 */

public class ObjServer extends ObjPersistent {
    
	private int serverPort;
	private Ctx asyncCtx;
	
	private HashMap<String,ObjContext> bindings=new HashMap<String,ObjContext>();
	private ServerMainLoop serverMainLoop;

    public ObjServer(int serverPort, Ctx asyncCtx) {
    	this.serverPort=serverPort;
    	this.asyncCtx=asyncCtx;
    }
    
    @Override
    public String getPersistenceId() {
    	return "ObjServer:" + serverPort;
    }
   
    @Override
    public void initPersistentObj() {
    	this.add(new FunctionRootContext());
    	this.serverMainLoop=new ServerMainLoop(serverPort, theServer());
    	(new Thread(serverMainLoop)).start();
    }
    @Override
    public void cleanupOnExit() {
    	serverMainLoop.setShuttingDown();
        // wait for main loop to terminate
        for(;;) {
        	if (serverMainLoop.isCompleted()) break;
        	try {Thread.sleep(20);} catch (Exception ex) {}
        }
    }
    
    public void bind (String path, ObjContext context) {
    	bindings.put(path,context);
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "WebServer";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "WebServer";
    }
    
    private ObjServer theServer () {
        return this;
    }
    
    class FunctionRootContext extends Function {
        public String getName() {
            return "RootContext";
        }
        public String getShortDesc() {
            return "RootContext() - returns Context object for path '/'";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjContext(theServer(), "/"));
        }
    }
    
    
    /**
     * Callback method via ServerMainLoop -> ClientMain
     */
    public synchronized byte[] processGETRequest(ObjRequest request) throws Exception {
    	ObjContext context = bindings.get(request.getUrl());
    	if (context != null) {
    		ObjClosure closure = context.getClosureGET();
    		
    		if (closure != null) {
    			List<Value> params=new ArrayList<Value>();
    			params.add(new ValueObj(request));

    			Value output = closure.callClosure(asyncCtx.sub(), params);
    			
    			if (output instanceof ValueList) {
    				StringBuffer sb=new StringBuffer();
    				List<Value> lines = ((ValueList) output).getVal();
    				for (Value v:lines) {
    					sb.append(v.getValAsString());
    					sb.append("\n");
    				}
    				return sb.toString().getBytes("UTF-8");	
    			} else {
    				return output.getValAsString().getBytes("UTF-8");
    			}
    		}
    	}
    	//System.out.println("ObjServer.processRequest(url=" + url + ")");
    	return ("<html><body><p>" + (new Date()) + " method="+ request.getMethod() + " url=" + request.getUrl() + "</p>").getBytes();
    }
    

    /**
     * Callback method via ServerMainLoop -> ClientMain
     */
    public synchronized byte[] processPOSTRequest(ObjRequest request) throws Exception {
    	ObjContext context = bindings.get(request.getUrl());
    	if (context != null) {
    		ObjClosure closure = context.getClosurePOST();
    		
    		if (closure != null) {
    			List<Value> params=new ArrayList<Value>();
    			params.add(new ValueObj(request));

    			Value output = closure.callClosure(asyncCtx.sub(), params);
    			
    			if (output instanceof ValueList) {
    				StringBuffer sb=new StringBuffer();
    				List<Value> lines = ((ValueList) output).getVal();
    				for (Value v:lines) {
    					sb.append(v.getValAsString());
    					sb.append("\n");
    				}
    				return sb.toString().getBytes("UTF-8");	
    			} else {
    				return output.getValAsString().getBytes("UTF-8");
    			}
    		}
    	}
    	//System.out.println("ObjServer.processRequest(url=" + url + ")");
    	return ("<html><body><p>" + (new Date()) + " method="+ request.getMethod() + " url=" + request.getUrl() + "</p>").getBytes();
    }
    

}
