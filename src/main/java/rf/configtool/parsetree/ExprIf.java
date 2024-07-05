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

package rf.configtool.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueNull;

public class ExprIf extends ExprCommon {

    // The inline form uses expressions, while the "traditional" form
    // uses statements, which in turn may well be expressions, but also
    // everything else.
    
    private Expr bool, exprIf, exprElse;
    private Stmt stmtIf, stmtElse;
    
    public ExprIf (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("if","expected 'if' keyword'");

        ts.matchStr("(", "expected '(' following 'if");
        bool=new Expr(ts);
        if (ts.matchStr(")")) {
            // traditional syntax: if(expr) stmt [else stmt]
            stmtIf=Stmt.parse(ts);
            if (ts.matchStr("else")) stmtElse=Stmt.parse(ts);
        } else {
            // single function call syntax: if(expr,ifExpr[,elseExpr])
            ts.matchStr(",", "expected comma following boolean expr");
            exprIf=new Expr(ts);
            
            if (!ts.matchStr(")")) {
                ts.matchStr(",", "expected comma or ')' following true expr");
                exprElse=new Expr(ts);
                ts.matchStr(")", "expected ')' closing 'if' expression");
            }
        }
        
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        boolean b=bool.resolve(ctx).getValAsBoolean();
        Value result;
        if (b) {
            if (exprIf != null) {
                result = exprIf.resolve(ctx);
            } else {
                stmtIf.execute(ctx);
                result=ctx.pop();
                if (result==null) result=new ValueNull();
            }
        } else {
            if (exprElse != null) {
                result = exprElse.resolve(ctx);
            } else if (stmtElse != null) {
                stmtElse.execute(ctx);
                result=ctx.pop();
                if (result==null) result=new ValueNull();
            } else {
                result = new ValueNull();
            }
        }
        
        return result;
    }
}
