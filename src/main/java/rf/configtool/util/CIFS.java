package rf.configtool.util;

import java.io.*;


import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb1.smb1.NtlmPasswordAuthentication;

public class CIFS {
	
	public static String createSmbUrl (String domain, String username, String password, String hostPort, String path) {
		if (domain==null) domain=""; else domain=domain+";";
		if (username==null) username="";
		if (password==null) password=""; else password=":"+password;
		
		path=path.replace('\\', '/');
		if (!path.startsWith("/")) path="/"+path;

		if((username+password).length() > 0) password=password+"@";
		
		return "smb://"+domain+username+password+hostPort+path;
	}
	
	public static InputStream getInputStream (String url) throws Exception {
		return new SmbFileInputStream(new SmbFile(url));
		
	}
	
	public static OutputStream getOutputStream (String url) throws Exception {
		return new SmbFileOutputStream(new SmbFile(url));
	}

}