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

package rf.xlang.util;

public class TabUtil {

    public static String substituteTabs(String s, int count) {
        StringBuffer sb=new StringBuffer();
        for (int pos=0; pos<s.length(); pos++) {
            char c=s.charAt(pos);
            if (c=='\t') {
                int fill=(sb.length())%count;
                if (fill==0) fill=count;
                for (int i=0; i<fill; i++) sb.append(" ");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
