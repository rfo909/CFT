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

package rf.configtool.util;

import java.util.Comparator;
import java.util.List;
import java.io.File;

public class FileModifiedSort {

    public static void sort (List<File> files) {
        files.sort(getLastModifiedComparator());
    }

    public static Comparator<File> getLastModifiedComparator() {
        return new Comparator<File>() {
            public int compare(File a, File b) {
                long ia;
                try {
                    ia=a.lastModified();
                } catch (Exception ex) {
                    ia=0L;
                }
                long ib;
                try {
                    ib=b.lastModified();
                } catch (Exception ex) {
                    ib=0L;
                }
                if (ia>ib) return -1;
                if (ia==ib) return 0;
                return 1;
                
            }
        };
    }

}
