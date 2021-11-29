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

package rf.configtool.parsetree;

import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;

public abstract class Stmt extends LexicalElement {
    

    public Stmt (TokenStream ts) throws Exception {
        super(ts);
    }
    
    public static Stmt parse (TokenStream ts) throws Exception {
        
        // Shell interactive functionality
        if (ts.peekStr("cd")) {
            return new StmtCd(ts);
        }
        if (ts.peekStr("ls") || ts.peekStr("lsd") || ts.peekStr("lsf")) {
            return new StmtLs(ts);
        }
        if (ts.peekStr("cat") || ts.peekStr("edit") || ts.peekStr("more")) {
            return new StmtCatEditMore(ts);
        }
        if (ts.peekStr("touch")) {
        	return new StmtTouch(ts);
        }
        
        // --------
        
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
         if (ts.peekStr("printDebug")) {
            return new StmtPrintDebug(ts);
        }
        if (ts.peekStr("timeExpr")) {
            return new StmtTimeExpr(ts);
        }
    
        // otherwise it must be an expression
        return new StmtExpr(ts);
    }
    
    
    public abstract void execute (Ctx ctx) throws Exception;


}
