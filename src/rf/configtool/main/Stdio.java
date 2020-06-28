package rf.configtool.main;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 2020-02-28 RFO
 * 
 * This class makes standard in and out a little more abstract. The idea is to use multiple
 * sessions or background tasks, each with its own ObjGlobal instance, which contains one
 * Stdio object. 
 * 
 * For output, the Stdio object should buffer a certain amount of output, while for input,
 * it should block until receiving input from the outside.
 */
public class Stdio {

	public static final int CircBufferedOutput = 100;
	
    // stdin and stdout may be null (disconnected)
    private BufferedReader stdin;
    private PrintStream stdout;
    
    private List<String> bufferedInputLines=new ArrayList<String>();
    private String[] outputLines;
    private int outputPos=0;
    
    public Stdio(BufferedReader stdin, PrintStream stdout) {
        this.stdin=stdin;
        this.stdout=stdout;

        outputLines=new String[CircBufferedOutput];
    	for (int i=0; i<outputLines.length; i++) outputLines[i]="";
    	
    }

    public String getInputLine() throws Exception {
        if (!bufferedInputLines.isEmpty()) {
            return bufferedInputLines.remove(0);
        }
        return readLine();
    }
    
    private String readLine() throws Exception {
    	if (stdin == null) throw new Exception("stdin disconnected - no buffered lines");
    	return stdin.readLine();
    }
    
    
    public boolean hasBufferedInputLines() {
        return bufferedInputLines.size()>0;
    }

    /**
     * Used by the stdin() statement
     */
    public void addBufferedInputLine (String s) {
        bufferedInputLines.add(s);
    }
    
    public void clearBufferedInputLines() {
        bufferedInputLines.clear();
    }

    /**
     * Output text
     */
   
    private String currLine="";
    
    public void print (String x) {
    	if (stdout != null) {
    		stdout.print(currLine+x);
    		currLine="";
    	} else {
    		currLine += x;
    	}
    }
    
    public void println (String x) {
    	String s=currLine+x;
    	currLine="";
    	
		outputLines[outputPos]=s;
		outputPos = (outputPos+1)%outputLines.length;
		if (stdout != null) stdout.println(s);
    }

    public void println () {
        println("");
    }
}
