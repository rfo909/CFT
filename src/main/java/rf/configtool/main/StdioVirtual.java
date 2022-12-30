/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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
    
    private List<String> outputBuffer = new ArrayList<String>();
    private long isBlockedOnInputSince = 0L;
    
    public StdioVirtual (BufferedReader stdin) {
        super(stdin);
    }
   
    @Override
    public synchronized void println (String s) {
        //System.out.println("StdioVirtual.println -> " + s);
        outputBuffer.add(s);
    }
    
    
    public synchronized List<String> getAndClearOutputBuffer() {
        List<String> x=outputBuffer;
        outputBuffer=new ArrayList<String>();
        return x;
    }
    
    public synchronized boolean hasBufferedOutput() {
        return outputBuffer.size() > 0;
    }
    

    private static final long MIN_BLOCK_DELAY_MS = 100;
    
    public boolean isBlockedOnInput () {
    	if (isBlockedOnInputSince == 0L) return false;
    	if (System.currentTimeMillis() - isBlockedOnInputSince >= MIN_BLOCK_DELAY_MS) return true;
    	return false;
    }

   
    @Override
    public String getInputLine() throws Exception {
    	isBlockedOnInputSince = System.currentTimeMillis();
    	String line=super.getInputLine();
    	isBlockedOnInputSince=0L;
    	return line;
    }

    
}
