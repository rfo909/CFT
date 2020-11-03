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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 2020-11 RFO
 * 
 * In order to virtualize Stdio for multiple threads, created via StmtSpawn, we need
 * to separate real Stdio from virtual Stdio. The real Stdio is available via the
 * ObjGlobal objects, and for all code running in the foreground. 
 * 
 * StmtSpawn creates a Process object, with a virtual Stdio. The code does not know
 * the difference, as the virtual Stdio also received an input stream, it's just
 * that it is a pipe stored in the Thread object. 
 * 
 * This lets us send data to it one line at a time. The virtual Stdio object also buffers
 * output internally. Since care has been taken to only communicate lines (never partial
 * lines) formatting should be good.
 */
public abstract class Stdio {
	

    private List<String> bufferedInputLines=new ArrayList<String>();
    private BufferedReader stdin;
    
     public Stdio (BufferedReader stdin) {
    	this.stdin=stdin;
    }
    
    /**
     * Used by the stdin() statement
     */
    
    public synchronized String getInputLine() throws Exception {
        if (!bufferedInputLines.isEmpty()) {
            return bufferedInputLines.remove(0);
        }
        return readLine();
    }
    
    private synchronized String readLine() throws Exception {
        if (stdin == null) throw new Exception("stdin disconnected - no buffered lines");
        return stdin.readLine();
    }
    
    
    public synchronized boolean hasBufferedInputLines() {
        return bufferedInputLines.size()>0;
    }

    /**
     * Used by the stdin() statement
     */
    public synchronized void addBufferedInputLine (String s) {
        bufferedInputLines.add(s);
    }
    
    public synchronized void clearBufferedInputLines() {
        bufferedInputLines.clear();
    }
    
    public synchronized int getCachedInputLineCount() {
    	return bufferedInputLines.size();
    }
   
    /**
     * Output text
     */
   
    public abstract void println (String s);
    
    public void println () {
        println("");
    }
}
