package rf.configtool.main.runtime.lib.web;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import rf.configtool.main.Version;

public 	class ClientMain implements Runnable {
	private ObjServer objServer;
	
	private boolean completed=false;
	private Socket sock;
	
	public synchronized boolean isCompleted() {
		return completed;
	}
	private synchronized void setCompleted() {
		this.completed=true;
	}
	
	public ClientMain (Socket sock, ObjServer objServer) {
		this.sock=sock;
		this.objServer=objServer;
	}

	private void sendData (PrintWriter out, BufferedOutputStream outBinary, String contentType, byte[] data) throws Exception {
		out.println("HTTP/1.1 200 OK");
		out.println("Server: CFT " + Version.getVersion());
		out.println("Date: " + new Date());
		out.println("Content-type: " + contentType);
		out.println("Content-length: " + data.length);
		out.println(); 
		out.flush();
		
		outBinary.write(data);
		outBinary.flush();
		outBinary.flush();
	}
	
	public void run () {
		BufferedReader in = null; 
		PrintWriter out = null; 
		BufferedOutputStream outBinary = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream()); 
				// remember to flush out before using outBinary
			outBinary = new BufferedOutputStream(sock.getOutputStream());
			
			String input = in.readLine();

			StringTokenizer st = new StringTokenizer(input," ",false);
			String method = st.nextToken().toUpperCase(); // we get the HTTP method of the client
			String url = st.nextToken().toLowerCase();
			
			Map<String,String> headers = new HashMap<String,String>();
			for (;;) {
				String line=in.readLine();
				if (line==null) break;
				if (line.trim().length()==0) break;
				int colonPos=line.indexOf(':');
				if (colonPos <= 0) continue;
				
				String name=line.substring(0,colonPos).trim();
				String value=line.substring(colonPos+1).trim();
				
				headers.put(name, value);
				//System.out.println("HDR> " + line);
			}
			
			if (method.equals("GET")) {
				try {
					ObjRequest request=new ObjRequest(headers, method, url);
					byte[] data=objServer.processGETRequest(request);
					//System.out.println("Sending data=" + data.length+" bytes");
					sendData(out, outBinary, "text/html", data);
				} finally {
					sock.close();
				}
			} else {
				byte[] data=("<html><body><p>"+(new Date())+" invalid method " + method).getBytes("UTF-8");
				System.out.println("Sending invalid method message (" + method + ")");
				sendData(out, outBinary, "text/html", data);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		setCompleted();
	}
}