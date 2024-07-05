/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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
import java.io.Console;
import java.io.PrintStream;

/**
 * Actual input and output, connected to global stdin and stdout, hence no buffering of output lines.
 */
public class StdioReal extends Stdio {

    private PrintStream stdout;
    
    public StdioReal(BufferedReader stdin, PrintStream stdout) {
        super(stdin);
        this.stdout=stdout;
    }
    
 
    @Override
    public synchronized void println (String s) {
        stdout.println(s);
    }

//    public synchronized void print (String s) {
//        stdout.print(s);
//    }

    public String readPassword() {
        Console console=System.console();
        return new String(console.readPassword());
    }

}
