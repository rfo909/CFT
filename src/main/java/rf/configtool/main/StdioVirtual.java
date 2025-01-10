/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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
import java.util.ArrayList;
import java.util.List;

/**
 * 2020-11 RFO
 * 
 * When creating a thread using StmtSpawn, an instance of virtual Stdio is needed.
 * It buffers output internally, and should also block in input, which for a spawned
 * thread will be a pipe which we can send lines to from the outside. 
 * 
 */
public class StdioVirtual extends Stdio {

    private final String newline;
    private StringBuffer outputBuffer = new StringBuffer();
    private long isBlockedOnInputSince = -1L;

    public StdioVirtual (BufferedReader stdin) {
        super(stdin);
        newline=(java.io.File.separatorChar=='/' ? "\n" : "\r\n");
    }
   
    @Override
    public synchronized void println (String s) {
        //System.out.println("StdioVirtual.println -> " + s);
        outputBuffer.append(s);
        outputBuffer.append(newline);

    }
    
    @Override
    public synchronized void print (String s) {
        outputBuffer.append(s);
    }
    
    public synchronized String getAndClearOutputBuffer() {
        String str=outputBuffer.toString();
        outputBuffer=new StringBuffer();
        return str;
    }
    
    public synchronized boolean hasBufferedOutput() {
        return outputBuffer.length() > 0;
    }
    

    private static final long MIN_BLOCK_DELAY_MS = 100;
    
    public Long isBlockedOnInputSince () {
        if (isBlockedOnInputSince <= 0L) return null;
        return (Long) isBlockedOnInputSince;
    }

   
    @Override
    public String getInputLine() throws Exception {
        isBlockedOnInputSince = System.currentTimeMillis();
        String line=super.getInputLine();
        isBlockedOnInputSince=-1L;
        return line;
    }

    
}
