package rf.configtool.root;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Pipe {
	private PipedOutputStream out;
	private PipedInputStream in;

	public Pipe() throws Exception {
		out = new PipedOutputStream();
		in = new PipedInputStream(out);
	}

	public void write(byte[] buf, int off, int len) throws Exception {
		out.write(buf, off, len);
	}

	public void close() {
		try {
			in.close();
		} catch (Exception ex) {
		}
		try {
			out.close();
		} catch (Exception ex) {
		}
	}

	public InputStream getInputStream() {
		return in;
	}

	public OutputStream getOutputStream() {
		return out;
	}

}
