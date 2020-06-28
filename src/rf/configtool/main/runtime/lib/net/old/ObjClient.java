package rf.configtool.main.runtime.lib.net.old;

import java.io.File;
import java.net.ServerSocket;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjPersistent;

public class ObjClient extends ObjPersistent {
    
    private String addr;
    private int port;
    
    public ObjClient (String addr, int port) {
        this.addr=addr;
        this.port=port;
        
        add(new FunctionConnect());
    }
    
    protected Obj self() {
        return this;
    }

    @Override 
    public String getPersistenceId() {
        return "ObjClient: " + addr + ":" + port;
    }
    


    @Override
    public boolean eq(Obj x) {
        return x==this;
    }

    @Override
    public String getTypeName() {
        return "RemoteClient";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("RemoteClient: " + addr + ":" + port);
    }
        
    
    class FunctionConnect extends Function {
        public String getName() {
            return "connect";
        }
        public String getShortDesc() {
            return "connect() - connect to remote server";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            // Run in foreground
            (new ClientLoop(ctx.getStdio(), addr,port)).run();
            return new ValueObj(self());
        }
    }




    
}
