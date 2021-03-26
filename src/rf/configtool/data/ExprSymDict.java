/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parser.TokenStream;

/**
 * Match a list of identifiers (local variables), then execute them as expressions, and put
 * the result into a Dict object.
 */
public class ExprSymDict extends ExprCommon {
	
	private List<String> identifiers = new ArrayList<String>();
	
    public ExprSymDict (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("SymDict", "expected 'SymDict'");
        ts.matchStr("(", "expected '(' following 'SymDict'");
        boolean comma=false;
        while (!ts.matchStr(")")) {
        	if (comma) ts.matchStr(",","expected comma or ')'");
        	String ident=ts.matchIdentifier("expected identifier");
        	identifiers.add(ident);
        	comma=true;
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
    	ObjDict x=new ObjDict();
    	for (String name:identifiers) {
    		x.set(name,ctx.resolveExpr(name));
    	}
    	return new ValueObj(x);
    }
}
