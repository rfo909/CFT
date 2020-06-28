package rf.configtool.main.runtime.lib.net.old;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjPersistent;

public class ObjServer extends ObjPersistent {
    
    private int port;
    private ServerSocket serverSocket;
    private RemoteServerMainLoop serverLoop;
    
    
    public ObjServer (int port) {
        this.port=port;
        
        add(new FunctionInit());
    }
    
    protected ObjServer self() {
        return this;
    }

    @Override 
    public String getPersistenceId() {
        return "ObjServer: " + port;
    }
    
    @Override
    public void cleanupOnExit() {
        try {
            serverSocket.close();
        } catch (Exception ex) {
            // ignore
        }
    }

    @Override
    public boolean eq(Obj x) {
        return x==this;
    }

    @Override
    public String getTypeName() {
        return "RemoteServer";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("RemoteServer: " + port);
    }
        

    class FunctionInit extends Function {
        public String getName() {
            return "init";
        }
        public String getShortDesc() {
            return "init() - set up listen socket and create processing loop, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("No parameters expected");
            
            if (serverLoop != null) {
                ctx.outln("Already running ... listening on port " + port);
            } else {            
                serverSocket = new ServerSocket(port);
                serverLoop=new RemoteServerMainLoop(ctx.getStdio(), serverSocket, self());
                (new Thread(serverLoop)).start();
    
                ctx.outln("Server listening on port " + port);
            }
            return new ValueObj(self());
        }
    }

    
}
