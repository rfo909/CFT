package rf.configtool.main.runtime.lib.net.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.util.Hex;

public class PushLoop implements Runnable {
    
	private Stdio stdio;

    private String addr;
    private int port;
    private ObjFile objFile;
    private String remotePath;
    private String remoteName;
    
    public PushLoop (Stdio stdio, String addr, int port, ObjFile objFile, String remotePath, String remoteName) {
        this.stdio=stdio;
        this.addr=addr;
        this.port=port;
        this.objFile=objFile;
        this.remotePath=remotePath;
        this.remoteName=remoteName;
    }
    
    public void run() {
        Socket socket;
        InputStream in;
        OutputStream out;
        try {
            InetAddress address=InetAddress.getByName(addr);
            socket = new Socket(address, port);
            in=socket.getInputStream();
            out=socket.getOutputStream();
        } catch (Exception ex) {
            stdio.println("PushLoop: " + ex.getMessage());
            return;
        }
        
        BufferedReader br=new BufferedReader(new InputStreamReader(in));
        PrintStream ps=new PrintStream(out);
        
        File f=new File(objFile.getPath());

        InputStream data=null;
        try {
            data=new FileInputStream(f);
            
            ps.println("Op:Push");
            ps.println("Push.Path:" + remotePath);
            ps.println("Push.File:" + remoteName);
            ps.println("Push.Size:" + f.length());
            ps.println(); // terminate header
            
            long startTime=System.currentTimeMillis();
            byte[] buf=new byte[1024];
            for (;;) {
                int count=data.read(buf);
                if (count <= 0) break;
                ps.println(Hex.toHex(buf,count));
            }
            long elapsedTime=System.currentTimeMillis()-startTime;
            
            ps.println(); // end marker for controlled termination
            stdio.println();
            stdio.println(elapsedTime + "ms");
        } catch (Exception ex) {
        	stdio.println("PushLoop: " + ex.getMessage());
        } finally {
            try {data.close();} catch (Exception ex) {};
            try {out.flush();} catch (Exception ex) {};
            try {socket.close();} catch (Exception ex) {};
        }
    }

}
