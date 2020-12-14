package rf.configtool.util.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class IO {

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private int timeoutMs;

	public IO(Socket socket) throws Exception {
		this.socket = socket;
		in = socket.getInputStream();
		out = socket.getOutputStream();
		setTimeoutMs(1000); // default
	}
	
	public void setTimeoutMs(int ms) throws Exception {
		this.timeoutMs = ms;
	}

	private String readInputStringShort() {
		try {
			socket.setSoTimeout(timeoutMs);
			int len = in.read();
			if (len==-1) {
				throw new RuntimeException("Socket EOF");
			}
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
			return s;
		} catch (Exception ex) {
			return null;
		}
	}

	private void writeOutputStringShort(String s) throws Exception {
		byte[] buf = s.getBytes("UTF-8");
		int len = buf.length;
		if (len > 256)
			throw new Exception("String too long");
		out.write(len);
		out.write(buf);
	}

	public String readInputString() {
		try {
			String lenStr = readInputStringShort();
			int len = Integer.parseInt(lenStr);
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
			return s;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void writeOutputString(String s) throws Exception {
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
