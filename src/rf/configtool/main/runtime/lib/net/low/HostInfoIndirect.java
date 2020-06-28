package rf.configtool.main.runtime.lib.net.low;


/**
 * Information about indirectly known hosts
 */
public class HostInfoIndirect {
	
	private String id;
	private String name;
	private String viaId;

	public HostInfoIndirect(HostInfo x, String viaId) {
		this(x.getId(), x.getName(), viaId);
	}

	public HostInfoIndirect(String id, String name, String viaId) {
		this.id = id;
		this.name = name;
		this.viaId = viaId;
	}
	
	public HostInfoIndirect (BufIn buf) throws Exception {
		this.name=buf.getString();
		this.id=buf.getString();
		this.viaId=buf.getString();
	}
	
	public void emit (BufOut buf) throws Exception {
		buf.addString(name);
		buf.addString(id);
		buf.addString(viaId);
	}



	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getViaId() {
		return viaId;
	}

	

}
