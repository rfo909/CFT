package rf.configtool.main.runtime.lib.net.low;

import rf.configtool.main.runtime.lib.net.MyRandom;

/**
 * Information about the current host and the host at the other end of
 * each connection. For nested knowledge about hosts known to others, 
 * see HostInfoIndirect class.
 */
public class HostInfo {
	
	private String name;
	private String id;
	
	public HostInfo() {
		this.name = "";
		this.id = ""+System.currentTimeMillis() + "x" + MyRandom.getInstance().getPositiveInt(1000000);
	}
	
	public HostInfo (String id, String name) {
		this.id=id;
		this.name=name;
	}
	
	public HostInfo (BufIn buf) throws Exception {
		this.name=buf.getString();
		this.id=buf.getString();
	}
	
	public void emit (BufOut buf) throws Exception {
		buf.addString(name);
		buf.addString(id);
	}
	
	public boolean nameOk() {
		return (name.length() > 1);
	}
	
	public void updateName (String name) {
		this.name=name.trim();
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
	
	public String toString() {
		return name;
	}
	

}
