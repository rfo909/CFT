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
import rf.configtool.main.SourceException;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;

/**
 * The stack pop assignment has moved from being an Expr to being a Stmt, in order
 * that it not return anything. Expr always return a Value, but doing so in this
 * case means we can not pop more than one value of the data stack, ever.
 */
public class StmtAssign extends Stmt {

    private String varName;
    
    public StmtAssign (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("=>","expected '=>'");
        varName=ts.matchIdentifier("expected variable name");
    }

    public void execute (Ctx ctx) throws Exception {
        Value v=ctx.pop();
        if (ctx.isLoopVariable(varName)) throw new SourceException(getSourceLocation(), "Invalid assign: '" + varName + "' is a loop variable");
        ctx.getFunctionState().set(varName, v);
    }

}
