package rf.configtool.main.runtime.lib.net.low;

import java.util.*;

public class MessageIdResponse extends MessageIdRequest {
	
	
	public MessageIdResponse(HostInfo thisHost, List<HostInfoIndirect> indirectHosts) {
		super(thisHost, indirectHosts);
	}
	
	public MessageIdResponse (BufIn buf) throws Exception {
		super(buf);
	}
	
	@Override
	public byte getMId() {
		return Message.MIdResponse;
	}

}