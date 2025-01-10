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
import rf.configtool.main.runtime.Value;

public class StmtBreak extends Stmt {
    
    private Expr expr;

    public StmtBreak (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("break","expected 'break'");
        if (ts.matchStr("(")) {
            expr=new Expr(ts);
            ts.matchStr(")", "expected ')' closing break statement");
        }
    }
    
    public void execute (Ctx ctx) throws Exception {
        if (expr == null) {
            // unconditional break
            ctx.setBreakLoopFlag();
        } else {
            // conditional break
            Value v=expr.resolve(ctx);
            boolean breakLoop=v.getValAsBoolean();
            
            if (breakLoop) {
                ctx.setBreakLoopFlag();
            }
        } 
    }


}
