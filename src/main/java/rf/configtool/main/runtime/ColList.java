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

package rf.configtool.main.runtime;

import java.util.*;

/**
 * For formatting output as columns
 *
 */
public class ColList {
    
    public static final ColList list() {
        return new ColList();
    }
    
    private List<String> list=new ArrayList<String>();

    private ColList() {
        
    }
    
    public ColList regular(String value) {
        list.add(value);
        return this;
    }

    public ColList regular() {
        list.add("");
        return this;
    }

    public ColList status(String value) {
        list.add(value);
        return this;
    }

    public ColList status() {
        list.add("");
        return this;
    }

    public List<String> getCols() {
        return list;
    }
    
//  public String getSimpleDisplay() {
//      StringBuffer sb=new StringBuffer();
//      for (String s:list) {
//          s=s+" ";
//          while (s.length() % 8 != 0) s=s + " ";
//          sb.append(s);
//      }
//      return sb.toString();
//  }
//
    public String getCompactDisplay() {
        StringBuffer sb=new StringBuffer();
        boolean space=false;
        for (String s:list) {
            if (space) sb.append(" ");
            sb.append(s);
            space=true;
        }
        return sb.toString();
    }
    
    public String toString() {
        return getCompactDisplay();
    }
}
