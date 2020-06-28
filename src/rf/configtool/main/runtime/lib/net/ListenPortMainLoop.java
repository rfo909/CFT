package rf.configtool.main.runtime.lib.net;

import java.net.ServerSocket;
import java.net.Socket;

import rf.configtool.main.Stdio;

/**
 * Accepting connections on listen socket
 */
public class ListenPortMainLoop implements Runnable {
    
    private Stdio stdio;
    private SessionManager mgr;
    private ServerSocket serverSocket;
    private String accessCode;
    
    public ListenPortMainLoop (Stdio stdio, SessionManager mgr, ServerSocket serverSocket, String accessCode) {
        this.stdio=stdio;
        this.mgr=mgr;
        this.serverSocket=serverSocket;
        this.accessCode=accessCode;
    }
    

    public void run() {
        try {
            while (true) {
                try {
                    Socket tcpSocket=serverSocket.accept();
                    mgr.addTCPServerSessionSocket(tcpSocket, stdio, accessCode);
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

