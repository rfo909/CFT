package rf.configtool.main.runtime.lib.net;

import java.net.*;
import java.util.*;

import rf.configtool.main.Stdio;

public class ListenPort {

	private final int port;
	private final Stdio stdio;
	private final SessionManager mgr;
	
	private final String accessCode;
	private ServerSocket serverSocket;
	private ListenPortMainLoop serverLoop;
	
	public ListenPort(Stdio stdio, SessionManager mgr, int port) throws Exception {
		this.stdio=stdio;
		this.mgr=mgr;
		this.port=port;
		
		// create accessCode
		this.accessCode=createRandomCode();
		
        if (serverLoop == null) {
            serverSocket = new ServerSocket(port);
            serverLoop=new ListenPortMainLoop(stdio, mgr, serverSocket, accessCode);
            (new Thread(serverLoop)).start();
        }
	}
	
	private char randomChar (String s) {
		int x=MyRandom.getInstance().getPositiveInt(s.length());
		return s.charAt(x);
	}
	
	private String createRandomCode() {
		final String a="abcdefhkmnrstuvwxz";
		final String b="23456789";
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<3; i++) {
			sb.append(randomChar(a));
			sb.append(randomChar(b));
		}
		return sb.toString();
	}
	
	public int getPort() {
		return port;
	}
	
	public String getAccessCode() {
		return accessCode;
	}
	
    public void cleanupOnExit() {
        try {
            serverSocket.close();
        } catch (Exception ex) {
            // ignore
        }
    }
	
}

