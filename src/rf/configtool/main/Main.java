package rf.configtool.main;

import java.io.*;
import java.util.*;

import rf.configtool.main.runtime.*;

public class Main {
	
	private static final Version VERSION = new Version();
	
    public static void main (String[] args) {
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
    
    public Main (BufferedReader stdin, PrintStream stdout) {
    	this(stdin, stdout, null);
    }

    public Main (BufferedReader stdin, PrintStream stdout, String scriptName) {
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
        Runtime runtime=new Runtime(objGlobal);
    	objGlobal.setRuntime(runtime);
	}
    
    public void setInitialCommands (List<String> initialCommands) {
    	this.initialCommands=initialCommands;
    }
    
    public void inputLoop() {
    	stdio.println(VERSION.getVersion());

    	try {
            for (;;) {
                Runtime cp=objGlobal.getRuntime();
                
                if (cp==null) {
                    stdio.println("Runtime exit, cleaning up");
                    objGlobal.cleanupOnExit();
                    return;
                }
                
                if (!initialCommands.isEmpty()) {
                	String cmd=initialCommands.remove(0).trim();
                	cp.processInteractiveInput(cmd);
                	continue;
                }

                String pre="$";
                
                stdio.print(pre + " ");
                String line=stdio.getInputLine().trim();
                
            	cp.processInteractiveInput(line);
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
}
