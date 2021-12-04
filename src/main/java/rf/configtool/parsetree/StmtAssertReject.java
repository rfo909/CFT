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

package rf.configtool.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;

public class StmtAssertReject extends Stmt{
    
    private boolean isAssert;
    private Expr expr;

    public StmtAssertReject (TokenStream ts) throws Exception {
        super(ts);
        
        if (ts.matchStr("assert")) {
            isAssert=true;
        } else {
            ts.matchStr("reject","expected 'assert' or 'reject'");
            isAssert=false;
        }
        ts.matchStr("(", "expected '(' following assert/reject");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing assert/reject statement");
    }
    
    public void execute (Ctx ctx) throws Exception {
        Value v=expr.resolve(ctx);
        boolean x=v.getValAsBoolean();
        
        
        
        boolean abortProcessing=(isAssert && !x) || (!isAssert && x);
        if (abortProcessing) {
            ctx.setAbortIterationFlag();
        }
    }


}
