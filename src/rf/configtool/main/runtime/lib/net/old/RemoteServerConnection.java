package rf.configtool.main.runtime.lib.net.old;

import java.net.Socket;
import java.util.HashMap;

import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Runtime;
import rf.configtool.main.Stdio;
import rf.configtool.main.Main;

import java.io.*;

public class RemoteServerConnection implements Runnable {
    
    private Stdio stdio;
    private Socket socket;
    private ObjServer objServer;
    
    public RemoteServerConnection (Stdio stdio, Socket socket, ObjServer objServer) {
        this.stdio=stdio;
        this.socket=socket;
        this.objServer=objServer;
    }
    
    public void run() {
        InputStream in;
        OutputStream out;
        try {
            in=socket.getInputStream();
            out=socket.getOutputStream();
        } catch (Exception ex) {
            stdio.println("ServerConnectionLoop: " + ex.getMessage());
            return;
        }
        
        BufferedReader stdin=new BufferedReader(new InputStreamReader(in));
        PrintStream stdout=new PrintStream(out);
        
        Main main=new Main(stdin,stdout);
        main.inputLoop();
    }
    

}
