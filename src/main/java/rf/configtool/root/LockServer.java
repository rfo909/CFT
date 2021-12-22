package rf.configtool.root;

import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;

/**
 * Lock files work ok on Linux, but on windows they really don't.
 */
public class LockServer {
	public static final int PORT=2911;
	private ServerLoop serverLoop;
	
	public LockServer() {
		init();
	}
	
	public void setShuttingDown() {
		serverLoop.setShuttingDown();
	}
	
	private void init () {
		try {
			ServerSocket serverSocket=new ServerSocket(PORT);
			this.serverLoop=new ServerLoop(serverSocket);
			Thread t = new Thread(this.serverLoop);
			t.start();			
		} catch (Exception ex) {
			// port must be in use by another instance
		}	
	}
	
	private boolean communicate (String op, String lockName) {
		init();
		String selfId="" + Runtime.getRuntime().hashCode() + "XX" + Thread.currentThread().getId();
		Socket sock=null;
		try {
			sock=new Socket("localhost",PORT);

			BufferedReader br=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintStream ps=new PrintStream(sock.getOutputStream());
			
			ps.println(op + " " + lockName + " " + selfId);
			String s = br.readLine().trim();
			return s.equals("OK");
		} catch (Exception ex) {
			return false;
		} finally {
			try {sock.close();} catch (Exception ex) {};
		}
		
	}
	
	public boolean getLock (String lockName) {
		return communicate("GET", lockName);
	}
	
	public boolean releaseLock (String lockName) {
		return communicate("RELEASE", lockName);
	}
	
	public boolean obtainLock (String lockName, long timeoutMillis) {
		long start=System.currentTimeMillis();
		while (System.currentTimeMillis()-start < timeoutMillis) {
			if (getLock(lockName)) return true;
			try {
				Thread.sleep(( (int) Math.random()*50)+3);
			} catch (Exception ex) {
				// ignore
			}
		}
		return false;
	}
	
		
}
	
class ServerLoop implements Runnable {
	private ServerSocket serverSocket;
	private HashMap<String,String> locks=new HashMap<String,String>();
	private boolean shuttingDown=false;
	
	public ServerLoop (ServerSocket serverSocket) throws Exception {
		this.serverSocket=serverSocket;
		serverSocket.setSoTimeout(200);
	}
	
	public synchronized void setShuttingDown() {
		this.shuttingDown=true;
	}
	public void run () {
			while (!shuttingDown) {
				Socket clientSocket=null;
				try {
					clientSocket=serverSocket.accept();
					clientSocket.setSoTimeout(200);
					BufferedReader br=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					PrintStream ps=new PrintStream(clientSocket.getOutputStream());
					String[] line=br.readLine().trim().split(" ");
					
					String op=line[0];
					String lock=line[1];
					String id=line[2];
					
					String lockValue=locks.get(lock);
					
					boolean ok=false;
					if (op.equals("GET")) {
						if (lockValue==null) {
							locks.put(lock,id);
							ok=true;
						} else if (lockValue.equals(id)) {
							ok=true;
						} else {
							ok=false;
						}
					} else if (op.equals("RELEASE")) {
						if (lockValue != null && lockValue.equals(id)) {
							locks.remove(lock);
							ok=true;
						} else {
							ok=false;
						}
					}
					ps.println("" + (ok ? "OK" : "FAIL"));
					ps.flush();
				} catch (Exception ex) {
					// ignore
				} finally {
					if(clientSocket != null) try {clientSocket.close();} catch (Exception ex) {};
				}

		}
	}
}

