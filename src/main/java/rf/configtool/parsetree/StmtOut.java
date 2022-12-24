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

package rf.configtool.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class StmtOut extends Stmt {

    private Expr expr;
    
    public StmtOut (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("out","expected 'out'");
        ts.matchStr("(", "expected '(' following out");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing out(...)");
    }

    public void execute (Ctx ctx) throws Exception {
        ctx.getOutData().out(expr.resolve(ctx));
    }

}
