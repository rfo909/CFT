package rf.configtool.main.runtime.lib.net.old;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import rf.configtool.main.Stdio;

public class RemoteServerMainLoop implements Runnable {
    
    private Stdio stdio;
    private ServerSocket serverSocket;
    private ObjServer objServer;
    
    public RemoteServerMainLoop (Stdio stdio, ServerSocket serverSocket, ObjServer objServer) {
        this.stdio=stdio;
        this.serverSocket=serverSocket;
        this.objServer=objServer;
    }
    

    public void run() {
        try {
            while (true) {
                try {
                    Socket tcpSocket=serverSocket.accept();
                    Thread thread = new Thread(new RemoteServerConnection(stdio, tcpSocket, objServer));
                    thread.start();
                } catch (Exception ex) {
                    stdio.println("ServerLoop failure: " + ex.getMessage());
                    return;
                }
            }
        } finally {
            try {serverSocket.close();} catch (Exception ex) {};
        }
    }
}
