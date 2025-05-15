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
import rf.xlang.main.runtime.Value;

public class ExprAssign extends ExprCommon {

    private String varName;
    private Expr expr;
    
    public ExprAssign (TokenStream ts) throws Exception {
        super(ts);
        varName=ts.matchIdentifier("expected variable name");
        ts.matchStr("=","expected '='");
        expr=new Expr(ts);
       
    }

    public Value resolve (Ctx ctx) throws Exception {
        Value v=expr.resolve(ctx);
        ctx.setVariable(varName, v);
        return v;
    }

}
