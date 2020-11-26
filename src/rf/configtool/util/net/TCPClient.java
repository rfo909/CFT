package rf.configtool.util.net;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


public class TCPClient  {
    
    private String addr;
    private int port;
    private Socket socket;
    private IO io;
    
    public TCPClient (String addr, int port) throws Exception {
        this.addr=addr;
        this.port=port;
        
        System.out.println("TCPClient host=" + addr + " port=" + port);
        InetAddress address=InetAddress.getByName(addr);
        socket = new Socket(address, port);
        System.out.println("TCPClient socket ok");
        io=new IO(socket);
	}
    
    public IO getIO() {
    	return io;
    }



    
}