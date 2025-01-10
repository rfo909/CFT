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
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.root.Root;

public class Main {
    
    static class Args {
        private String[] args;
        private int pos;
        
        public Args (String[] args) { this.args=args; }
        
        public boolean hasNext() {
            return args.length > pos;
        }
        public String peek() {
            return args[pos];
        }
        public String get(String msg) throws Exception {
            if (!hasNext()) throw new Exception(msg);
            return args[pos++];
        }
    }
    
    public static void main (String[] argsArray) throws Exception {
        Args args=new Args(argsArray);
        
        if (args.hasNext()) {
            if (args.peek().equals("-version")) {
                System.out.println(Version.getVersion());
                System.exit(0);
            } else if (args.peek().equals("-help")) {
                System.out.println("Valid options:");
                System.out.println("  -version");
                System.out.println("  -help");
                System.out.println("  [FLAGS]? [scriptName [command-lines]]");
                System.out.println("[FLAGS]");
                System.out.println("  -d scriptDir");
                System.out.println("  -noterm     ## no proper terminal - when called remotely");
                System.exit(0);
            }
        }
        
        String scriptDir=null;
        boolean noTerminal = false;
        
        while(args.hasNext() && args.peek().startsWith("-")) {
            if (args.peek().equals("-d")) {
                args.get("advance past known -d");
                scriptDir=args.get("scriptDir following -d");
            } else if (args.peek().equals("-noterm")) {
                args.get("comsume -noterm");
                noTerminal=true;
            } else {
                System.out.println("Invalid flag. Run CFT with -help.");
                System.exit(1);
            }
        }

            
        BufferedReader stdin=new BufferedReader(new InputStreamReader(System.in));
        PrintStream stdout=System.out;
        
        StdioReal stdio=new StdioReal(stdin, stdout);
        Root root=new Root(stdio, scriptDir, noTerminal);
        
        if (args.hasNext()) {
            String scriptName=args.get("scriptname");
            root.loadScript(scriptName);
        }
        
        List<String> commands=new ArrayList<String>();
        while (args.hasNext()) {
            commands.add(args.get("command-string"));
        }
        for (String line:commands) root.addInitialCommand(line);
        
        root.inputLoop();
    }
 
    
}
