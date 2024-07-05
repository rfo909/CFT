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

public abstract class Stmt extends LexicalElement {
    

    public Stmt (TokenStream ts) throws Exception {
        super(ts);
    }
    
    public static Stmt parse (TokenStream ts) throws Exception {
        
        if (ts.peekStr("->")) {
            return new StmtIterate(ts);
        }
        if (ts.peekStr("loop")) {
            return new StmtLoop(ts);
        }
        if (ts.peekStr("assert") || ts.peekStr("reject") || ts.peekStr("continue")) {
            return new StmtAssertRejectContinue(ts);
        }
        if (ts.peekStr("break")) {
            return new StmtBreak(ts);
        }
        if (ts.peekStr("out")) {
            return new StmtOut(ts);
        }
        if (ts.peekStr("condOut")) {
            return new StmtCondOut(ts);
        }
        if (ts.peekStr("report")) {
            return new StmtReport(ts);
        }
        if (ts.peekStr("reportList")) {
            return new StmtReportList(ts);
        }
        if (ts.peekStr("help")) {
            return new StmtHelp(ts);
        }
         if (ts.peekStr("addDebug")) {
            return new StmtAddDebug(ts);
        }
        if (ts.peekStr("timeExpr")) {
            return new StmtTimeExpr(ts);
        }
        if (ts.peekStr("setBreakPoint")) {
            return new StmtSetBreakPoint(ts);
        }
        if (ts.peekStr("=>")) {
            return new StmtAssign(ts);
        }
    
        // otherwise it must be an expression
        return new StmtExpr(ts);
    }
    
    
    public abstract void execute (Ctx ctx) throws Exception;


}
