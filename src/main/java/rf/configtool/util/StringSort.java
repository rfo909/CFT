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

package rf.configtool.util;

import java.util.Comparator;
import java.util.List;

public class StringSort {

    public static void sort (List<String> data) {
        Comparator<String> c=new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        };
        data.sort(c);
    }
}
