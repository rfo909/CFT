package rf.configtool.main.runtime.lib.net.low;

import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.lib.net.MyRandom;
import rf.configtool.main.runtime.lib.net.SessionManager;

/**
 * Protocol processing for a connection where we are the server
 */
public class ProtocolManagerServerSide extends ProtocolManager implements Runnable {
	
	private long lastCheck=0L;
	private boolean shuttingDown=false;
	
	
	public ProtocolManagerServerSide (SessionManager mgr, Stdio stdio, HostInfo thisHost, Sender sender, Receiver receiver) throws Exception {
		super(mgr, stdio, thisHost, sender, receiver);
	}
	
	@Override
	public synchronized boolean processProtocolMessage (Message message) throws Exception {
		if (message instanceof MessageIdResponse) {
			MessageIdResponse resp = ((MessageIdResponse) message);
			setRemoteHost(resp.getHost());
			addIndirectHosts(resp.getIndirectHosts());
			return true;
		}
		if (message instanceof MessageClose) {
			String reason=((MessageClose) message).getReason();
			System.out.println("Shutting down connection: reason: " + reason);
			shuttingDown=true;
		}
		
		return false;
	}
	
	@Override
	public void closeConnection (String reason) throws Exception {
		getSender().sendMessage(new MessageClose(reason));
		shuttingDown=true;
	}


	@Override
	public boolean mustCloseConnection() {
		return shuttingDown;
	}



	@Override
	public void run() {
		try {
			Thread.sleep(3000); 
				// let client wait a bit, to both block it from testing different 
				// access-codes in rapid succession, and also to ensure it is ready
				// to receive the pings below
			
			// send ping to the client - if it comes through with correct content,
			// the client uses the correct accessCode, otherwise it should close the
			// connection.
			for (int i=0; i<10; i++) {
				Thread.sleep(200);
				//System.out.println("Sending Ping");
				getSender().sendMessage(new MessagePing());
			}
			
			while (!mustCloseConnection()) {
				
				// identify remote host regularly
				if (System.currentTimeMillis() - lastCheck > 1000L) {
					// get info from client
					getSender().sendMessage(new MessageIdRequest(getThisHost(), getIndirectHosts()));
					lastCheck=System.currentTimeMillis();
					continue;
				}
				
				// avoid tight looping
				Thread.sleep(50);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
