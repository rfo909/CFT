package rf.configtool.main.runtime.lib.net.low;

/**
 * This message always contains a single int value, with a static value. When a new
 * client connects to a server, this message is sent a number of times, allowing the
 * client to decide if encryption (and access-code) is correct.
 *
 */
public class MessagePing extends Message {

	public static final int IntValue=1234567890;
	
	private int value;

	public MessagePing() {
		this.value=IntValue;
	}
	
	public MessagePing (BufIn buf) throws Exception {
		value=buf.getInt();
		buf.matchEOF();
	}
	
	public boolean isCorrect() {
		return value==IntValue;
	}
	
	@Override
	public byte getMId() {
		return Message.MPing;
	}
	
	@Override
	public byte[] getSendBytes() {
		BufOut buf=new BufOut();
		buf.addInt(value);
		return buf.getBytes();
	}
}