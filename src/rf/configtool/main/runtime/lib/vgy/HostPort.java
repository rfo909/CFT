package rf.configtool.main.runtime.lib.vgy;

public class HostPort {
	private String hostPort;
	private String host;
	private int port;
	
	public HostPort (String hostPort) throws Exception {
		this.hostPort=hostPort;
		int pos=hostPort.indexOf(':');
		host=hostPort.substring(0,pos);
		port=Integer.parseInt(hostPort.substring(pos+1));
	}
	
	public String getHostPort() {
		return hostPort;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	

}
