package rf.configtool.main.runtime.lib.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjClosure;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjPersistent;

/**
 * Web server
 */

public class ObjServer extends ObjPersistent {
    
    private int serverPort;
    private Ctx asyncCtx;
    private File serverLogFile;
    
    private HashMap<String,ObjContext> bindings=new HashMap<String,ObjContext>();
    private ServerMainLoop serverMainLoop;

    public ObjServer(int serverPort, Ctx asyncCtx) {
        this.serverPort=serverPort;
        this.asyncCtx=asyncCtx;

        this.add(new FunctionRootContext());
        this.add(new FunctionSetServerLogFile());;
    }
    
    @Override
    public String getPersistenceId() {
        return "ObjServer:" + serverPort;
    }
   
    @Override
    public void initPersistentObj() {
        
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

    /**
     * Service function 
     */
    public void bind (String path, ObjContext context) {
        bindings.put(path,context);
    }

    /**
     * Service function
     */
    public synchronized void appendToServerLog (String context, List<String> lines) {
        if (serverLogFile==null) return;
        
        
        try {
            PrintStream ps=null;
            try {
                ps=new PrintStream(new FileOutputStream(serverLogFile,true), false, "UTF-8");
                ps.println();
                ps.println(""+(new Date()));
                for (String line: lines) ps.println("[" + context + "] " + line);
            } finally {
                if (ps != null) try {ps.close();} catch (Exception ex) {};
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    /**
     * Service function
     */
    public synchronized void appendToServerLog (String context, String line) {
        List<String> lines=new ArrayList<String>();
        lines.add(line);
        appendToServerLog(context, lines);
    }


    /**
     * Service function
     */
    public synchronized void appendToServerLog (String context, String line, Throwable t)  {
        List<String> lines=new ArrayList<String>();
        lines.add(line);
        lines.add(t.getMessage());
        for (StackTraceElement e : t.getStackTrace()) {
            lines.add(e.toString());
        }
        appendToServerLog(context, lines);
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
    
    class FunctionSetServerLogFile extends Function {
        public String getName() {
            return "setServerLogFile";
        }
        public String getShortDesc() {
            return "setServerLogFile(file) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected file parameter");
            Obj obj=getObj("file",params,0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected file parameter");
            serverLogFile=((ObjFile) obj).getFile();

            return new ValueObj(theServer());
        }
    }
    

    
    
    
    /**
     * Callback method via ServerMainLoop -> ClientMain
     */
    public synchronized ResponseData processGETRequest(ObjRequest request) throws Exception {
        ObjContext context = bindings.get(request.getUrl());
        if (context != null) {
            ObjClosure closure = context.getClosureGET();
            String contentType=context.getContentType();
            
            if (closure != null) {
                List<Value> params=new ArrayList<Value>();
                params.add(new ValueObj(request));

                Value output = closure.callClosure(asyncCtx.sub(), params);
                byte[] data = createBytesFromValue(output);
                return new ResponseData(contentType, data);
            }
        }
        //System.out.println("ObjServer.processRequest(url=" + url + ")");
        return new ResponseData(("<html><body><p>" + (new Date()) + " method="+ request.getMethod() + " url=" + request.getUrl() + "</p>").getBytes());
    }
    

    /**
     * Callback method via ServerMainLoop -> ClientMain
     */
    public synchronized ResponseData processPOSTRequest(ObjRequest request) throws Exception {
        ObjContext context = bindings.get(request.getUrl());
        if (context != null) {
            ObjClosure closure = context.getClosurePOST();
            String contentType=context.getContentType();
            
            if (closure != null) {
                List<Value> params=new ArrayList<Value>();
                params.add(new ValueObj(request));

                Value output = closure.callClosure(asyncCtx.sub(), params);
                byte[] data = createBytesFromValue(output);
                return new ResponseData(contentType, data);
            }
        }
        //System.out.println("ObjServer.processRequest(url=" + url + ")");
        return new ResponseData(("<html><body><p>" + (new Date()) + " method="+ request.getMethod() + " url=" + request.getUrl() + "</p>").getBytes());
    }
    
    
    private byte[] createBytesFromValue (Value output) throws Exception {
        if (output instanceof ValueList) {
            StringBuffer sb=new StringBuffer();
            List<Value> lines = ((ValueList) output).getVal();
            for (Value v:lines) {
                sb.append(v.getValAsString());
                sb.append("\n");
            }
            return sb.toString().getBytes("UTF-8");
        }
        if (output instanceof ValueString) {
            return ((ValueString) output).getVal().getBytes("UTF-8");
        }
        if (output instanceof ValueBinary) {
            return ((ValueBinary) output).getVal();
        }
        // all else
        return output.getValAsString().getBytes("UTF-8");
    }


}
