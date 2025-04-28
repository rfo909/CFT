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

package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;

/**
 * Lookup of identifier or call of function
 */
public class DottedCall extends LexicalElement {

    private String ident;
    private List<Expr> params=new ArrayList<Expr>();
    
    public DottedCall (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr(".","Expected .identifier");
        ident=ts.matchIdentifier("expected .identifier");
        if (ts.matchStr("(")) {
            boolean comma=false;
            while (!ts.peekStr(")")) {
                if (comma) {
                    ts.matchStr(",", "expected comma");
                }
                params.add(new Expr(ts));
                comma=true;
            }
            ts.matchStr(")", "expected ')' closing param list");
        }
    }
    
    public Value resolve (Ctx ctx, Obj obj) throws Exception {

        return null;  // TODO

    }
}
