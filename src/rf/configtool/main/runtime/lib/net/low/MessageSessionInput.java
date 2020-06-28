package rf.configtool.main.runtime.lib.net.low;

/**
 * Send command input to remote target
 */
public class MessageSessionInput extends Message {

	private String line; 
	
	public MessageSessionInput(String line) {
		this.line=line;
	}
	
	public MessageSessionInput (BufIn buf) throws Exception {
		line=buf.getString();
		buf.matchEOF();
	}
	
	
	public String getLine() {
		return line;
	}

	@Override
	public byte getMId() {
		return Message.MSessionInput;
	}
	
	@Override
	public byte[] getSendBytes() throws Exception {
		BufOut buf=new BufOut();
		buf.addString(line);
		return buf.getBytes();
	}
}
