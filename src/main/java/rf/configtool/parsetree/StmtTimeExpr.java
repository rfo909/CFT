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

package rf.configtool.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class StmtTimeExpr extends Stmt {

    private Expr expr;
    
    public StmtTimeExpr (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("timeExpr","expected 'timeExpr'");
        ts.matchStr("(", "expected '(' following timeExpr");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing timeExpr(...)");
    }

    public void execute (Ctx ctx) throws Exception {
        final long exprInit=ctx.getObjGlobal().getExprCount();
        final long a=System.currentTimeMillis();
        expr.resolve(ctx);  // ## Not pushing result on stack, to avoid cluttering up the output
        long duration=System.currentTimeMillis()-a;
        long exprCount = ctx.getObjGlobal().getExprCount() - exprInit;
        String ePerSec="";
        if (duration>0) {
            ePerSec=" / per sec: "+(1000*exprCount/duration);
        }
        
        ctx.getStdio().println("Time: " + (duration) + " ms");
        ctx.getStdio().println("Expr: " + exprCount + ePerSec);
    }

}
