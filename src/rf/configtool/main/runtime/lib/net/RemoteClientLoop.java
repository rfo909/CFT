package rf.configtool.main.runtime.lib.net;

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

public class RemoteClientLoop implements Runnable {
    
    private Stdio stdio;

    private String addr;
    private int port;
    
    public RemoteClientLoop (Stdio stdio, String addr, int port) {
        this.stdio=stdio;
        this.addr=addr;
        this.port=port;
    }
    
    public void run() {
        Socket socket=null;
        InputStream in;
        OutputStream out;
        try {
            InetAddress address=InetAddress.getByName(addr);
            socket = new Socket(address, port);
            in=socket.getInputStream();
            out=socket.getOutputStream();
        
        
	        BufferedReader remoteStdout=new BufferedReader(new InputStreamReader(in));
	        PrintStream remoteStdin=new PrintStream(out);
	        
            String linePrefix="[" + addr + "] ";

            System.out.print(linePrefix); // initial linePrefix

            (new Thread(new OutputThread(remoteStdout, stdio, linePrefix))).start();
            
            for (;;) {
                String line=stdio.getInputLine();
                if (line.equals(":quit")) {
                    return; // close connection 
                }
                System.out.print(linePrefix); // following newline included in readLine
                remoteStdin.println(line);
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        } finally {
        	if (socket != null) try {socket.close();} catch (Exception ex) {};
        }
        
    }
    
    class OutputThread implements Runnable {
        private BufferedReader remoteStdout;
        private Stdio stdio;
        private String prefix;
        
        public OutputThread (BufferedReader remoteStdout, Stdio stdio, String prefix) {
            this.remoteStdout=remoteStdout;
            this.stdio=stdio;
            this.prefix=prefix;
        }
        
        public void run() {
            try {
                for(;;) {
                	String line=remoteStdout.readLine();
                	stdio.println(prefix+line);
                }
            } catch (Exception ex) {
                System.out.println("<Remote-Output-EOF>");
            }
        }
    }

}
