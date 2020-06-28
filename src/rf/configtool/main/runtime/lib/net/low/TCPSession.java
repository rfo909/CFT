package rf.configtool.main.runtime.lib.net.low;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.lib.net.SessionManager;


public class TCPSession {
	private SessionManager mgr;
	private Socket socket;
	private Stdio stdio;
	
	private final InputStream in;
	private final OutputStream out;
	
	private ProtocolManager pm;
	
	public TCPSession (SessionManager mgr, HostInfo thisHost, Socket socket, Stdio stdio, String accessCode, boolean isServer) throws Exception {
		this.mgr=mgr;
		
		this.socket=socket;
		this.stdio=stdio;
		
		Crypto crypto=new Crypto(accessCode);
		
		this.in=socket.getInputStream();
		this.out=socket.getOutputStream();
		
		Sender sender=new Sender(crypto, out);
		Receiver receiver=new Receiver(crypto, in);
		
		if (isServer) {
			pm=new ProtocolManagerServerSide(mgr, stdio, thisHost, sender, receiver);
		} else {
			pm=new ProtocolManagerClientSide(mgr, stdio, thisHost, sender, receiver);
		}

		// start background threads
		new Thread(pm).start();
		
		new Thread(sender).start();
		new Thread(receiver).start();
	}
	
	public ProtocolManager getProtocolManager() {
		return pm;
	}
	
	public void closeConnection(String reason) throws Exception {
		pm.closeConnection(reason);
	}


	public void cleanupOnExit() {
		try {
			socket.close();
		} catch (Exception ex) {
			// ignore
		}
	}

}