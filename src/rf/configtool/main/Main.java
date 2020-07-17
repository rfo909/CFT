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
    
    public static void main (String[] args) throws Exception {
        BufferedReader stdin=new BufferedReader(new InputStreamReader(System.in));
        PrintStream stdout=System.out;
        
        Stdio stdio=new Stdio(stdin, stdout);
        Root root=new Root(stdio);
        
        if (args.length >= 1) {
            String scriptName=args[0];
            root.loadScript(scriptName);
        }
        
        List<String> commands=new ArrayList<String>();
        for (int i=1; i<args.length; i++) {
            commands.add(args[i]);
        }
        root.setInitialCommands(commands);
        
        root.inputLoop();
    }
 
    
}
