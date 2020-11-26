package rf.configtool.util.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class IO {

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private int timeoutMs;
	private String id="";

	public IO(Socket socket) throws Exception {
		this.socket = socket;
		in = socket.getInputStream();
		out = socket.getOutputStream();
		setTimeoutMs(1000); // default
	}
	
	public void setId(String id) {
		this.id=id;
	}

	public void setTimeoutMs(int ms) throws Exception {
		this.timeoutMs = ms;
	}

	private String readInputStringShort() {
		try {
			socket.setSoTimeout(timeoutMs);
			int len = in.read();
			byte[] buf = new byte[len];
			int received = 0;
			while (received < len) {
				socket.setSoTimeout(timeoutMs);
				int count = in.read(buf, received, len - received);
				if (count < 0)
					throw new Exception("Socket read failed");
				received += count;
			}
			String s = new String(buf, "UTF-8");
			System.out.println(id+"IO:readShort = " + s);
			return s;
		} catch (Exception ex) {
			return null;
		}
	}

	private void writeOutputStringShort(String s) throws Exception {
		System.out.println(id+"IO:writeShort = " + s);
		byte[] buf = s.getBytes("UTF-8");
		int len = buf.length;
		if (len > 256)
			throw new Exception("String too long");
		out.write(len);
		out.write(buf);
	}

	public String readInputString() {
		System.out.println(id+"IO:read");
		try {
			String lenStr = readInputStringShort();
			System.out.println(id+"IO:read parseInt on lenStr " + lenStr);
			int len = Integer.parseInt(lenStr);
			System.out.println(id+"IO:read len=" + len);
			byte[] buf = new byte[len];
			int received = 0;
			
			while (received < len) {
				socket.setSoTimeout(timeoutMs);
				int count = in.read(buf, received, len - received);
				if (count < 0)
					throw new Exception("Socket read failed");
				received += count;
				System.out.println(id+"IO:read received=" + received);
			}
			String s = new String(buf, "UTF-8");
			System.out.println(id+"IO:read = " + s);
			return s;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void writeOutputString(String s) throws Exception {
		System.out.println(id+"IO:write = " + s);
		byte[] buf = s.getBytes("UTF-8");
		int len = buf.length;
		writeOutputStringShort("" + len);
		out.write(buf);
	}

	public void close() {
		try {
			socket.close();
		} catch (Exception ex) {
		}
	}

}
