package rf.configtool.util.net;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {
    
    private int port;
    private ServerSocket serverSocket;
    
    private List<TCPServerConnection> connections=new ArrayList<TCPServerConnection>();
    
    public synchronized void addConnection (TCPServerConnection x) {
        connections.add(x);
    }
    
    public synchronized TCPServerConnection getConnection () {
        if (connections.isEmpty()) return null;
        return connections.remove(0);
    }
    
    public TCPServer (int port) throws Exception {
        this.port=port;
        serverSocket=new ServerSocket(port);
        TCPServerLoop serverLoop=new TCPServerLoop(serverSocket, this);
        (new Thread(serverLoop)).start();
    }
    
    
}
