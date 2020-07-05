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

package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;

public abstract class Stmt extends LexicalElement {
    

    public Stmt (TokenStream ts) throws Exception {
        super(ts);
    }
    
    public static Stmt parse (TokenStream ts) throws Exception {
        if (ts.peekStr("cd")) {
            return new StmtCd(ts);
        }
        if (ts.peekStr("=")) {
            return new StmtAssign(ts);
        }
        if (ts.peekStr("->")) {
            return new StmtIterate(ts);
        }
        if (ts.peekStr("loop")) {
            return new StmtLoop(ts);
        }
        if (ts.peekStr("assert") || ts.peekStr("reject")) {
            return new StmtAssertReject(ts);
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
        if (ts.peekStr("stdin")) {
            return new StmtStdin(ts);
        }
        if (ts.peekStr("showCode")) {
            return new StmtShowCode(ts);
        }
        if (ts.peekStr("debug")) {
            return new StmtDebug(ts);
        }
    
        // otherwise it must be an expression
        return new StmtExpr(ts);
    }
    
    
    public abstract void execute (Ctx ctx) throws Exception;


}
