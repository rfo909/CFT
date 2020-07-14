/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main;

import java.io.BufferedReader;
import java.io.*;
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

    public static final int CircBufferedOutput = 1000;
    
    // stdin and stdout may be null (disconnected)
    private BufferedReader stdin;
    private PrintStream stdout;
    
    private List<String> bufferedInputLines=new ArrayList<String>();
    private String[] outputLines;
    private int outputPos=0;
    
    public Stdio (InputStream in, OutputStream out) {
    	this.stdin=new BufferedReader(new InputStreamReader(in));
    	this.stdout=new PrintStream(out);

    	outputLines=new String[CircBufferedOutput];
        for (int i=0; i<outputLines.length; i++) outputLines[i]="";
    }
    
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
   
    public void println (String s) {
        outputLines[outputPos]=s;
        outputPos = (outputPos+1)%outputLines.length;
        if (stdout != null) stdout.println(s);
    }

    public void println () {
        println("");
    }
}
