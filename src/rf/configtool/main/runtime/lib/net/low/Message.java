package rf.configtool.main.runtime.lib.net.low;

public abstract class Message {
	
	public static final byte MPing = 10;
	
	public static final byte MIdRequest = 20;
	public static final byte MIdResponse = 21;
	public static final byte MIdClose = 21;
	
	
	public static final byte MSessionInput = 30;
	
	
	public abstract byte getMId();

	public abstract byte[] getSendBytes() throws Exception;
	
	
	public static Message parse (byte mId, byte[] data) throws Exception {
		BufIn buf=new BufIn();
		buf.addData(data);
		
		if (mId==MPing) {
			return new MessagePing(buf);
		}
		
		if (mId==MIdRequest) {
			return new MessageIdRequest(buf);
		}
		if (mId==MIdResponse) {
			return new MessageIdResponse(buf);
		}
		
		throw new Exception("Invalid mId " + mId);
	}
}
