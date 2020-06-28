package rf.configtool.main.runtime.lib.net.low;

import java.io.*;
import java.util.*;

public class Sender implements Runnable {
	
	private Crypto crypto;
	private OutputStream out;
	private ProtocolManager pm;

	public Sender (Crypto crypto, OutputStream out) {
		this.crypto=crypto;
		this.out=out;
	}
	
	public void setProtocolManager (ProtocolManager pm) {
		this.pm=pm;
	}

	private List<Message> sendList=new ArrayList<Message>();

	public synchronized void sendMessage (Message m) {
		sendList.add(m);
	}
	
	private synchronized Message getQueuedMessage() {
		if (sendList.isEmpty()) return null;
		return sendList.remove(0);
	}
	
	private void send (byte[] buf) throws Exception {
		out.write(buf);
	}
	
	private void send (Message m) throws Exception {
		byte mId=m.getMId();
		BufOut header=new BufOut();
		header.addByte(mId);
		
		byte[] body=m.getSendBytes();
		int len=body.length;
		header.addInt(len);
		
		// using len+mId as offset for crypto engine, to produce unpredictable results
		byte[] data=crypto.encrypt(body, len+mId);
		
		send(header.getBytes());
		send(data);
	}
	
	public void run() {
		try {
			while (!pm.mustCloseConnection()) {
				Message m=getQueuedMessage();
				if (m==null) {
					Thread.sleep(100);
				} else {
					send(m);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				Thread.sleep(2000);
				out.close();
			} catch (Exception ex) {
				// ignore
			}
		}
		System.out.println("% Sender terminating");

	}
}
