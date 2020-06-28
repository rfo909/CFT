package rf.configtool.main.runtime.lib.net.low;

import java.util.*;

public class BufOut {
	
	private List<Byte> data=new ArrayList<Byte>();
	
	public void addByte (byte b) {
		data.add(b);
	}
	
	public void addInt (int i) {
		addByte( (byte) ( (i & 0xFF000000) >> 24) );
		addByte( (byte) ( (i & 0x00FF0000) >> 16) );
		addByte( (byte) ( (i & 0x0000FF00) >> 8) );
		addByte( (byte) ( (i & 0x000000FF)) );
	}
	
	public void addString(String s) throws Exception {
		byte[] data=s.getBytes("UTF-8");
		
		int len=data.length;
		addInt(len);
		
		for (int i=0; i<len; i++) addByte(data[i]);
	}
	
	public byte[] getBytes() {
		byte[] arr=new byte[data.size()];
		for (int i=0; i<arr.length; i++) {
			arr[i]=data.get(i);
		}
		return arr; 
	}
	
}
