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

import rf.configtool.lexer.TokenStream;

public class ClassDetails {
    
    private String name;
    private String type;
    
    /**
     * Parse /class Ident ... string
     */
    public ClassDetails (TokenStream ts) throws Exception {
        final String msg="expected '/class Name [as Type]'";
        if (!ts.matchStr("//")) { // allow double slash, but ignore it
            ts.matchStr("/", msg + " - expected '/'");
        }
        ts.matchStr("class", msg + " - expected keyword 'class'");
        name=ts.matchIdentifier(msg + " - expected Name");
        if (ts.matchStr("as")) {
            type=ts.matchIdentifier(msg + " - expected Type");
        }
    }
    
    
    /**
     * Generate /class Ident ... string
     */
    public String createClassDefString () {
        return "/class " + name + (type != null ? " as " + type : "");
    }
    
    
    public ClassDetails (String name, String type) {
        this.name=name;
        this.type=type;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        if (type==null) return name;
        return type;
    }

}
