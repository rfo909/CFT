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
import java.util.*;

import rf.configtool.main.runtime.*;
import rf.configtool.root.Root;

public class Main {
    
    private static final Version VERSION = new Version();
    
    public static void main (String[] args) throws Exception {
        BufferedReader stdin=new BufferedReader(new InputStreamReader(System.in));
        PrintStream stdout=System.out;
        
        Main m;
        
        if (args.length >= 1) {
            String scriptName=args[0];
            m=new Main(stdin,stdout,scriptName);
        } else {
            m=new Main(stdin,stdout);
        }
        
        List<String> commands=new ArrayList<String>();
        for (int i=1; i<args.length; i++) {
            commands.add(args[i]);
        }
        m.setInitialCommands(commands);
        
        m.inputLoop();
    }
    
    private Stdio stdio;
    private ObjGlobal objGlobal;
    private List<String> initialCommands;
    
    public static final boolean TEST_ROOT = false;
    
    public Main (BufferedReader stdin, PrintStream stdout) throws Exception {
    		this(stdin, stdout, null);
    }

    public Main (BufferedReader stdin, PrintStream stdout, String scriptName) throws Exception {
    	if (TEST_ROOT) {
    		// EXPERIMENTAL
	    	Root root=new Root(new Stdio(stdin,stdout));
	    	root.inputLoop();
	    	System.exit(0);
    	} 
        
        stdio=new Stdio(stdin, stdout);
        objGlobal=new ObjGlobal(stdio);
        if (scriptName != null) {
            try {
                objGlobal.loadCode(scriptName);
            } catch (Exception ex) {
                stdout.println("Could not load script '" + scriptName + "'");
                return;
            }
        }
    }
    
    public void setInitialCommands (List<String> initialCommands) {
        this.initialCommands=initialCommands;
    }
    
    public void inputLoop() {
        copyrightNotice();
        stdio.println(VERSION.getVersion());

        try {
            for (;;) {
                
                if (objGlobal.getTerminationFlag()) {
                    stdio.println("Runtime exit, cleaning up");
                    objGlobal.cleanupOnExit();
                    return;
                }
                
                Runtime cp=objGlobal.getRuntime();
                if (!initialCommands.isEmpty()) {
                    String cmd=initialCommands.remove(0).trim();
                    cp.processInteractiveInput(cmd);
                    continue;
                }

                String pre="$";
                
                System.out.print(pre + " ");
                String line=stdio.getInputLine().trim();
                
                cp.processInteractiveInput(line);
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    

    private void copyrightNotice() {
        stdio.println("");
        stdio.println("CFT (\"ConfigTool\") Copyright (c) 2020 Roar Foshaug");
        stdio.println("This program comes with ABSOLUTELY NO WARRANTY. See GNU GPL3.");
        stdio.println("This is free software, and you are welcome to redistribute it");
        stdio.println("under certain conditions. See GNU GPL3.");
        stdio.println("");
        stdio.println("You should have received a copy of the GNU General Public License");
        stdio.println("along with this program.  If not, see <https://www.gnu.org/licenses/>");
        stdio.println("");
    }
   
    
}
