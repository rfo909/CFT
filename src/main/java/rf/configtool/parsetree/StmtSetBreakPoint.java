/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;

public class StmtSetBreakPoint extends Stmt {

    private Expr value;
    
    public StmtSetBreakPoint (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("setBreakPoint","expected 'setBreakPoint'");
        ts.matchStr("(", "expected '(' following setBreakPoint");
        value=new Expr(ts);
        ts.matchStr(")", "expected ')' closing setBreakPoint stmt");
    }

    public void execute (Ctx ctx) throws Exception {
        Stdio stdio=ctx.getStdio();
        String loc = this.getSourceLocation().toString();
        String msg=value.resolve(ctx).getValAsString();
        
        ValueList stack = ctx.getStdio().peekFullCFTStackTrace();
        
        stdio.println("### setBreakPoint: " + msg + " " + loc);
        for (Value line : stack.getVal()) {
        	stdio.println(" CALL-HISTORY: " + line.getValAsString());
        }
        stdio.println("---");
        stdio.println("Enter 'y' to terminate");
        String line = stdio.getInputLine();
        if (line.trim().toLowerCase().equals("y")) {
        	throw new Exception("Aborted at breakbpoint: " + msg);
        }
        
    }

}
