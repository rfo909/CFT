package rf.configtool.util.net;

import java.net.*;
import java.io.*;

public class TCPServerLoop implements Runnable {
    
    private ServerSocket serverSocket;
    private TCPServer tcpServer; 
    
    public TCPServerLoop (ServerSocket serverSocket, TCPServer tcpServer) {
        this.serverSocket=serverSocket;
        this.tcpServer=tcpServer;
    }
    
    public void run() {
        for (;;) {
            try {
                Socket socket = serverSocket.accept();
                tcpServer.addConnection(new TCPServerConnection(socket));
            } catch (Exception ex) {
                return;
            }
        }
    }

}
