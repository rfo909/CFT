/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

package rf.configtool.main.runtime.lib.web;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMainLoop implements Runnable {
    private int serverPort;
    private ObjWebServer objServer;
    
    private boolean shuttingDown=false;
    private boolean completed=false;
    private List<ClientMain> pendingClients=new ArrayList<ClientMain>();
    
    private int clientCounter=0;
    
    public ServerMainLoop (int serverPort, ObjWebServer objServer) {
        this.serverPort=serverPort;
        this.objServer=objServer;
    }
    
    public synchronized void setShuttingDown() {
        shuttingDown=true;
    }
    public synchronized boolean isCompleted() {
        return completed;
    }
    private synchronized void setCompleted() {
        this.completed=true;
    }
    public void run () {
        ServerSocket serverSocket=null;
        try {
            serverSocket = new ServerSocket(serverPort);
            serverSocket.setSoTimeout(100);
                // using timeout to enable orderly termination and cleanup
            
            // we listen until user halts server execution
            while (!shuttingDown) {
                Socket clientSocket=null;
                try {
                    clientSocket=serverSocket.accept();
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    clientSocket=null;
                }
                if (clientSocket != null) {
                    ClientMain client = new ClientMain(clientSocket, objServer, clientCounter++);
                    pendingClients.add(client);
                    //System.out.println("pendingClient=" + pendingClients.size());
                    Thread thread = new Thread(client);
                    thread.start();
                }
                // remove completed clients from internal list
                while(!pendingClients.isEmpty()) {
                    ClientMain c=pendingClients.get(0);
                    if (c.isCompleted()) pendingClients.remove(0); else break;
                }
            }
        } catch (IOException e) {
            System.out.println("Server listen socket: " + e.getMessage());
        } 
        
        // Cleanup - wait for pending clients
        while (!pendingClients.isEmpty()) {
            ClientMain c=pendingClients.get(0);
            if (c.isCompleted()) {
                pendingClients.remove(0);
            } else try {
                Thread.sleep(20);
            } catch(Exception ex) {
                // ignore
            }
        }
        
        // all clients have completed
        if (serverSocket != null) {
            try {
                serverSocket.close(); 
            } catch (Exception ex) {
                // ignore
            }
        }
        
        System.out.println(getClass().getName() + " on port " + serverPort + " shut down ok");

        // terminate
        setCompleted();
    }
}
