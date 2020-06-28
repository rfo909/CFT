package rf.configtool.main.runtime.lib.net.low;

import java.util.*;

/**
 * Process input until either identifying a data package (header or body) or there
 * is an exception. If there is an exception, call abortRead() and wait for more
 * data to be received, before trying again. If no problem, call commitRead() to
 * purge those data from the internal buffer.
 * 
 * Also used to process isolated message.
 * 
 * @author roar
 *
 */
public class BufIn {

	private List<Byte> buf=new ArrayList<Byte>();
	private int readCount=0;
	
	public BufIn () {
	}
	
	public synchronized void addData (byte[] data, int count) {
		for (int i=0; i<count; i++) {
			buf.add(data[i]);
		}
	}
	
	public synchronized int getByteCount() {
		return buf.size();
	}
	
	public synchronized void addData (byte[] data) {
		addData(data, data.length);
	}
	
	public synchronized byte getByte() {
		return buf.get(readCount++);
	}
	
	public synchronized void abortRead() {
		readCount=0;
	}
	
	public synchronized void matchEOF() throws Exception {
		if (readCount != buf.size()) throw new Exception("Not at EOF, readCount=" + readCount + " buf.size()=" + buf.size());
	}
	
	public synchronized void commitRead() {
		while(readCount>0) {
			buf.remove(0);
			readCount--;
		}
	}
	public int getInt() {
		int a=(getByte() & 0xFF) << 24;
		int b=(getByte() & 0xFF) << 16;
		int c=(getByte() & 0xFF) << 8;
		int d=(getByte() & 0xFF);
		return (a | b | c | d);
	}
	
	public String getString() throws Exception {
		int len=getInt();
		byte[] buf=new byte[len];
		for (int i=0; i<len; i++) {
			buf[i]=getByte();
		}
		return new String(buf,"UTF-8");
	}
	
	public static void main (String[] args) {
//		int i=129;
//		byte b=(byte) (i & 0xFF);
//		int j=(int) (b & 0xFF);
//		System.out.println(i + " -> " + j);
		try {
			for (int testValue=1201; testValue<1205; testValue++) {
				BufOut out=new BufOut();
				out.addInt(testValue);
				BufIn in=new BufIn();
				in.addData(out.getBytes());
				int result=in.getInt();
				System.out.println(testValue + " -> " + result);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
