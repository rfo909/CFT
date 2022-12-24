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

package rf.configtool.main.runtime.lib;

/**
 * Protecting files and directories from unintended operations
 */
public class Protection {

    
    public static Protection NoProtection = new Protection();  // private constructor
    
    private String code;
    
    private Protection() {
        code=null;
    }
    
    public Protection(String code) {
        if (code==null) this.code="-";
        else this.code=code;
    }
    
    public boolean isActive() {
        return (code != null);
    }
    
    public String getCode() {
        return code;
    }

    public void validateDestructiveOperation (String op, String element) throws Exception {
        if (!isActive()) return; // ok
        throw new Exception("INVALID-OP " + op + " : " + element + " (PROTECTED: " + code + ")");
    }
}
