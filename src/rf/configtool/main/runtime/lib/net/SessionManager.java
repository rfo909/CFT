package rf.configtool.main.runtime.lib.net;

import java.net.Socket;
import java.io.*;

import rf.configtool.main.OutText;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.lib.net.low.HostInfo;
import rf.configtool.main.runtime.lib.net.low.HostInfoIndirect;
import rf.configtool.main.runtime.lib.net.low.TCPSession;
import java.util.*;


/**
 * Manages all network sessions, from both listen ports, and dedicated connections.
 * Each consists of two threads, for input and output. The differentiation into
 * client and server is important to decide who sends what, during initialization,
 * where we need to verify access and enable encryption and for the broadcasts that
 * are used to create the routing info. 
 */
public class SessionManager {
	
	private final HostInfo thisHost;
	private List<TCPSession> sessions=new ArrayList<TCPSession>();
	
	// Routing info is split into the hosts that we reach directly, at the other
	// end of a TCPSession, and those that are reached indirectly via some
	// sequence of hosts, starting with a direct host.
	private Map<String,HostInfo> directHosts=new HashMap<String,HostInfo>();
	private Map<String,HostInfoIndirect> indirectHosts=new HashMap<String,HostInfoIndirect>();
	
	private Map<String,Long> hostTimestamps=new HashMap<String,Long>();
	
	
	public SessionManager(HostInfo thisHost) {
		this.thisHost=thisHost;
	}


	public void report (OutText out) {
		
		out.addReportData("this-host", thisHost.getName());
		
		
		Iterator<String> dh=directHosts.keySet().iterator();
		while (dh.hasNext()) {
			String key=dh.next();
			HostInfo host=directHosts.get(key);
			
			out.addReportData("direct-host", host.getName());
		}
		

		Iterator<String> idh=indirectHosts.keySet().iterator();
		while (idh.hasNext()) {
			String key=idh.next();
			HostInfoIndirect host=indirectHosts.get(key);

			out.addReportData("indirect-host", host.getName());

		}

//		for (TCPSession s:sessions) {
//			HostInfo remoteHost = s.getProtocolManager().getRemoteHost();
//			if (remoteHost != null) {
//				out.addReportData("listen-server", remoteHost.getName());
//			}
//			List<HostInfoIndirect> iHosts = s.getProtocolManager().getIndirectHosts();
//			for (HostInfoIndirect x:iHosts) {
//				out.addReportData("connection-to-server", x.getName());
//			}
//		}
	}
	
	public void addTCPClientSessionSocket (Socket socket, Stdio stdio, String accessCode) throws Exception {
		boolean isServer=false;
		sessions.add(new TCPSession(this, thisHost, socket, stdio, accessCode, isServer));
	}
	
	public void addTCPServerSessionSocket (Socket socket, Stdio stdio, String accessCode) throws Exception {
		boolean isServer=true;
		sessions.add(new TCPSession(this, thisHost, socket, stdio, accessCode, isServer));
	}
		
	public void cleanupOnExit() {
	   for (TCPSession sess:sessions) {
		   try {
			   sess.closeConnection("<" + thisHost.getName() + ": exit>");
		   } catch (Exception ex) {
			   // ignore
		   }
	   }
	   try {
		   Thread.sleep(1000);
	   } catch (Exception ex) {
		   // ignore
	   }
	   
	   for (TCPSession sess:sessions) {
		   sess.cleanupOnExit();
	   }
    }

	
	public List<HostInfoIndirect> getIndirectHosts() {
		List<HostInfoIndirect> list=new ArrayList<HostInfoIndirect>();
		Iterator<String> keys=indirectHosts.keySet().iterator();
		while (keys.hasNext()) {
			String key=keys.next();
			list.add(indirectHosts.get(key));
		}
		keys=directHosts.keySet().iterator();
		while (keys.hasNext()) {
			String key=keys.next();
			list.add(new HostInfoIndirect(directHosts.get(key), thisHost.getId()));
		}
		return list;
	}
	
	public void addIndirectHosts (List<HostInfoIndirect> list) {
		for (HostInfoIndirect x:list) {
			if (x.getId().equals(thisHost.getId())) continue;
			if (directHosts.get(x.getId()) != null) continue;
			indirectHosts.put(x.getId(), x);
			hostTimestamps.put(x.getId(), System.currentTimeMillis());
		}
		checkHostTimeouts();
	}
	
	public void addDirectRemoteHost (HostInfo host) {
		if (host.getId().equals(thisHost.getId())) {
			System.out.println("*** addDirectRemoteHost == thisHost - ignoring");
			return;
		}
		directHosts.put(host.getId(), host);
		hostTimestamps.put(host.getId(), System.currentTimeMillis());

		if (indirectHosts.get(host.getId()) != null) {
			// no need for indirect knowledge of a host which has now
			// become a directly connected host
			indirectHosts.remove(host.getId());
		}
		
		checkHostTimeouts();
	}
	

	final long HostTimeout = 10000L;
	
	private void checkHostTimeouts() {
		Iterator<String> keys=hostTimestamps.keySet().iterator();
		while (keys.hasNext()) {
			String key=keys.next();
			long age=System.currentTimeMillis() - hostTimestamps.get(key);
			if (age > HostTimeout) {
				HostInfo x=directHosts.get(key);
				if (x != null) {
					directHosts.remove(key);
				}
			}
		}
		
		// verify expired indirect hosts, plus reachability 
		Iterator<String> iKeys=indirectHosts.keySet().iterator();
		while (iKeys.hasNext()) {
			String key=iKeys.next();
			long age=System.currentTimeMillis() - hostTimestamps.get(key);
			if (age > HostTimeout) {
				System.out.println("Indirect host " + indirectHosts.get(key).getName() + " timed out");
				indirectHosts.remove(key);
				continue;
			}
			if (!verifyHostCanBeReached(key)) {
				System.out.println("Indirect host " + indirectHosts.get(key).getName() + " can not be reached. Removing it.");
				indirectHosts.remove(key);
			}
		}
	}
    
	private boolean verifyHostCanBeReached (String key) {
		if (directHosts.get(key) != null) return true;
		HostInfoIndirect x=indirectHosts.get(key);
		if (x==null) return false;
		return verifyHostCanBeReached(x.getViaId());
	}
}

