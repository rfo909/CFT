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
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;

/**
 * Lookup of identifier or call of function
 */
public class ExprLookupOrCall extends ExprCommon {

    private String ident;
    private List<Expr> params=new ArrayList<Expr>();
    private ExprLookupOrCall next;
    
    public ExprLookupOrCall (TokenStream ts) throws Exception {
        super(ts);
        ident=ts.matchIdentifier("expected lookup identifier");
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
        if (ts.matchStr(".")) {
            next=new ExprLookupOrCall(ts);
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        return null;
    }
    
    private Value callFunction(Function f, Ctx ctx, List<Value> values) throws Exception {
        try {
            return f.callFunction(ctx, values);
        } catch (Exception ex) {
            throw new SourceException(getSourceLocation(), ex);
        }
    }
}
