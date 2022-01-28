/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

package rf.configtool.main.runtime.stats;

import java.util.*;
import java.io.*;

public class MemberFunctions {

    public static String FILE = "Stats.txt";
    private static List<String> buffer=new ArrayList<String>();
    
    public static void call (String className, String funcName) {
        buffer.add(className + "|"+funcName);
        if (buffer.size() >= 100) sync();
    }
    
    private static void sync() {
        PrintStream ps=null;
        try {
            File f=new File(FILE);
            ps=new PrintStream(new FileOutputStream(f,true));
            for (String line:buffer) {
                ps.println(line);
            }
            buffer.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (ps != null) try {ps.close();} catch (Exception e) {}
        }
    }
    
}
