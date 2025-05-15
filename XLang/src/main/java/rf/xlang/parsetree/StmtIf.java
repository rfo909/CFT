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

import rf.xlang.lexer.TokenStream;
import rf.xlang.main.Ctx;

public class StmtIf extends Stmt {

    // The inline form uses expressions, while the "traditional" form
    // uses statements, which in turn may well be expressions, but also
    // everything else.
    
    private Expr bool;
    private Stmt stmtIf, stmtElse;
    
    public StmtIf (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("if","expected 'if' keyword'");

        ts.matchStr("(", "expected '(' following 'if");
        bool=new Expr(ts);
        ts.matchStr(")","expected ')'");
        stmtIf=Stmt.parse(ts);
        if (ts.matchStr("else")) stmtElse=Stmt.parse(ts);
    }
    
    public void execute (Ctx ctx) throws Exception {
        boolean b=bool.resolve(ctx).getValAsBoolean();
        if (b) {
            stmtIf.execute(ctx);
        } else {
            if (stmtElse != null) {
                stmtElse.execute(ctx);
            }
        }
    }
}
