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

package rf.configtool.main.runtime.lib;

public class Regex {
    
    public static String createRegex(String pattern, boolean caseSensitive) {
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<pattern.length(); i++) {
            Character c=pattern.charAt(i);
            if (c=='*') {
                sb.append(".*");
            } else if (c=='?') {
                sb.append(".");
            } else if (c=='\\') {
                sb.append("[\\\\]");
            } else if (".^?[]{}()+-".indexOf(c) >= 0) {
                   sb.append("[\\"+c+"]");
            } else {
                if (caseSensitive) {
                    sb.append(c);
                } else {
                    String s=""+c;
                    sb.append("["+s.toLowerCase()+s.toUpperCase()+"]");
                }
            }
            
        }
        return sb.toString();
    }
    
    public static String createGlobRegex (String pattern, boolean caseSensitive) {
        return "^" + Regex.createRegex(pattern, caseSensitive) + "$";
    }
    
}
