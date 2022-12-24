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

/**
 * For formatting output. Cols with same names are lined up if possible
 */
public class Col {
    
    public static Col regular(String value) {
        return new Col(value, true);
    }
    public static Col status(String value) {
        return new Col(value, false);
    }
    
    private String value;
    private boolean trunc;
    
    private Col (String value, boolean trunc) {
        this.value=value;
        this.trunc=trunc;
    }
    public String getValue() {
        return value;
    }
    public boolean isTrunc() {
        return trunc;
    }
    
    
}
