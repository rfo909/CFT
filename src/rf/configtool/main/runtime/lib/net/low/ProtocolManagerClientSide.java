package rf.configtool.main.runtime.lib.net.low;

import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.lib.net.SessionManager;

/**
 * Protocol processing for a connection where we are the client
 */
public class ProtocolManagerClientSide extends ProtocolManager implements Runnable {

	private boolean badPingReceived=false;
	private boolean shuttingDown=false;

	public ProtocolManagerClientSide (SessionManager mgr, Stdio stdio, HostInfo thisHost, Sender sender, Receiver receiver) {
		super(mgr, stdio, thisHost, sender, receiver);
	}
	
	@Override
	public synchronized boolean processProtocolMessage (Message message) throws Exception {
		
		if (message instanceof MessagePing) {
			badPingReceived = !((MessagePing) message).isCorrect();
			return true;
		}
		
		if (message instanceof MessageIdRequest) {
			MessageIdRequest req = ((MessageIdRequest) message);
			setRemoteHost(req.getHost());
			addIndirectHosts(req.getIndirectHosts());
			getSender().sendMessage(new MessageIdResponse(getThisHost(), getIndirectHosts()));
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
		return badPingReceived || shuttingDown;
	}

	
	@Override
	public void run() {
		try {
			while (!mustCloseConnection()) {
				// do work
					// TODO?
				
				// avoid tight looping
				Thread.sleep(50);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
