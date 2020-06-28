package rf.configtool.main.runtime.lib.net.low;

import java.util.*;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.lib.net.SessionManager;

public abstract class ProtocolManager implements Runnable {
	
	private SessionManager mgr;

	private Sender sender;
	
	private Stdio stdio;
	
	private final HostInfo thisHost;
	private HostInfo remoteHost;
	
	public ProtocolManager (SessionManager mgr, Stdio stdio, HostInfo thisHost, Sender sender, Receiver receiver) {
		this.mgr=mgr;
		this.thisHost=thisHost;
		this.sender=sender;
		this.stdio=stdio;
		receiver.setProtocolManager(this);
		sender.setProtocolManager(this);
	}
	
	/**
	 * The "higher-ups" have decided to close this connection.
	 */
	public abstract void closeConnection (String reason) throws Exception; 

	
	/**
	 * Receiver and sender should continously check this value, and close down when it
	 * returns true
	 */
	public abstract boolean mustCloseConnection();
	
	
	
	public Sender getSender() {
		return sender;
	}
	
	public HostInfo getThisHost() {
		return thisHost;
	}
	
	public HostInfo getRemoteHost() {
		return remoteHost;
	}
	
	public void setRemoteHost (HostInfo remoteHost) {
		this.remoteHost=remoteHost;
		mgr.addDirectRemoteHost(remoteHost);
	}

	public List<HostInfoIndirect> getIndirectHosts() {
		return mgr.getIndirectHosts();
	}
	
	public void addIndirectHosts (List<HostInfoIndirect> list) {
		mgr.addIndirectHosts(list);
	}


	/**
	 * Callback method following receiver.setProtocolManager(this). The method implements
	 * functionality that is common to network hosts, regardless of whether they connected
	 * to the network by listening for connections (server) or connecting to listener (client).
	 */
	public final void processMessage (Message message) throws Exception {
		// first check if the message is protocol spesific
		if (processProtocolMessage(message)) {
			return; 
		}
		
		// TODO: implement common functionality for
		// - creating remote session
		// - file transfers
		// - copy and invoke remote scripts
		// - ???
		
		System.out.println("Unknown message: " + message.getClass().getName());
	}

	/**
	 * Subclass protocol managers for clients and servers must implement this
	 */
	public abstract boolean processProtocolMessage (Message message) throws Exception;
	
	
	
	/**
	 * This method is required by sub-classes, as we implement Runnable. The
	 * run method is a forever-loop that perform background activities which
	 * require regular sending of messages to maintain the information about
	 * the network, etc. All received data are routed via processMessage(), which
	 * should fall back to processCommonMessages() after checking for protocol
	 * management messages.
	 */
	public abstract void run();
	
	
}
