package rf.configtool.util.net;

import java.net.Socket;
import java.io.*;

public class TCPServerConnection {
    
    private Socket socket;
    private IO io;
    
    
    public TCPServerConnection (Socket socket) throws Exception {
        this.socket=socket;
        io=new IO(socket);
        io.setTimeoutMs(1000); // default

    }
    
    public IO getIO() {
    	return io;
    }
  
}