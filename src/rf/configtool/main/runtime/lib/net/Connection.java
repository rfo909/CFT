package rf.configtool.main.runtime.lib.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import rf.configtool.main.Stdio;

public class Connection {

	private String host;
	private int port;
	private Socket socket;
	
	public Connection(Stdio stdio, SessionManager mgr, String host, int port, String accessCode) throws Exception {
		this.host = host;
		this.port = port;
		
		InetAddress address=InetAddress.getByName(host);
	    socket = new Socket(address, port);
	    
	    mgr.addTCPClientSessionSocket(socket, stdio, accessCode);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

    public void cleanupOnExit() {
    	try {
    		socket.close();
    	} catch (Exception ex) {
    		// ignore
    	}
    }
    
}
