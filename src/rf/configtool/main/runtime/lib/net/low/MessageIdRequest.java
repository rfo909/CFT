package rf.configtool.main.runtime.lib.net.low;

import java.util.*;

/**
 * Sent from server side, querying clients about their name and id. The response is
 * identical, but with different message id. The point is to avoid a broadcast storm,
 * allowing only server-sides to initiate, and client-sides to respond.
 */
public class MessageIdRequest extends Message {

	private HostInfo host;
	private List<HostInfoIndirect> indirectHosts;
	
	public MessageIdRequest(HostInfo thisHost, List<HostInfoIndirect> indirectHosts) {
		this.host=thisHost;
		this.indirectHosts=indirectHosts;
		if (indirectHosts==null) throw new RuntimeException("indirectHosts==null");
		//System.out.println("Creating message with " + indirectHosts.size() + " indirect hosts");
	}
	
	public MessageIdRequest (BufIn buf) throws Exception {
		host=new HostInfo(buf);
		
		indirectHosts=new ArrayList<HostInfoIndirect>();
		int knownHostsCount=buf.getInt();
		//System.out.println("Parsing message with knownHostsCount=" + knownHostsCount);
		for (int i=0; i<knownHostsCount; i++) {
			indirectHosts.add(new HostInfoIndirect(buf));
			
		}
		buf.matchEOF();
	}
	
	
	public HostInfo getHost() {
		return host;
	}

	public List<HostInfoIndirect> getIndirectHosts() {
		return indirectHosts;
	}

	@Override
	public byte getMId() {
		return Message.MIdRequest;
	}
	
	@Override
	public byte[] getSendBytes() throws Exception {
		BufOut buf=new BufOut();
		host.emit(buf);
		buf.addInt(indirectHosts.size());
		for (HostInfoIndirect x:indirectHosts) {
			x.emit(buf);
		}
		return buf.getBytes();
	}
}
