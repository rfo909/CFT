package rf.configtool.main.runtime.lib.net.low;

import java.io.*;

public class Receiver implements Runnable {
	
	private Crypto crypto;
	private InputStream in;
	private ProtocolManager pm;
	private BufIn bufIn=new BufIn();
	
	public Receiver (Crypto crypto, InputStream in) {
		this.crypto=crypto;
		this.in=in;
	}
	
	public void setProtocolManager (ProtocolManager pm) {
		this.pm=pm;
	}
	
	
	private void processMessage (byte mId, byte[] data) throws Exception {
		try {
			Message m=Message.parse(mId, data);
			pm.processMessage(m);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public void run() {
		byte[] buf = new byte[16*1024];
		try {
			while (!pm.mustCloseConnection()) {
				// blocking read prevents this instance from tight looping
				
				int count=in.read(buf);
				if (count < 0) {
					throw new Exception("read failed");
				}
				bufIn.addData(buf,count);
				
				// process as many messages as possible
				Messages: for (;;) {
					byte mId=0;
					byte[] body=null;
					try {
						// read message header
						mId=bufIn.getByte();
						int len=bufIn.getInt();
						
						// read encrypted message data
						byte[] data=new byte[len];
						for (int i=0; i<len; i++) {
							data[i]=bufIn.getByte();
						}
						
						// getting here without exception, means we got a message
						bufIn.commitRead();
						
						// using len+mId as offset for crypto engine, to produce unpredictable results
						body=crypto.decrypt(data, len+mId);

					} catch (Exception ex) {
						bufIn.abortRead();
						break Messages;
					}
					
					if (body != null) {
						processMessage(mId, body);
					}
					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				Thread.sleep(2000);
				in.close();
			} catch (Exception ex) {
				// ignore
			};
		}

		System.out.println("% Receiver terminating");
		
	
	}


}
