package rf.configtool.main.runtime.lib.net.low;

/**
 * When a connection is closed, the other end is notified via this message, providing
 * a controlled shutdown.
 *
 */
public class MessageClose extends Message {

	private String reason;
	
	public MessageClose(String reason) {
		this.reason=reason;
	}
	
	public MessageClose (BufIn buf) throws Exception {
		reason=buf.getString();
		buf.matchEOF();
	}
	
	public String getReason() {
		return reason;
	}
	
	@Override
	public byte getMId() {
		return Message.MIdClose;
	}
	
	@Override
	public byte[] getSendBytes() throws Exception {
		BufOut buf=new BufOut();
		buf.addString(reason);
		return buf.getBytes();
	}
}