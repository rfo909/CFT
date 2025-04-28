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

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public abstract class Stmt extends LexicalElement {
    

    public Stmt (TokenStream ts) throws Exception {
        super(ts);
    }
    
    public static Stmt parse (TokenStream ts) throws Exception {
        
        if (ts.peekStr("loop")) {
            return new StmtLoop(ts);
        }
        if (ts.peekStr("if")) {
            return new StmtIf(ts);
        }
        if (ts.peekStr("break")) {
            return new StmtBreak(ts);
        }
        if (ts.peekStr("return")) {
            return new StmtReturn(ts);
        }
        
    
        // otherwise it must be an expression
        return new StmtExpr(ts);
    }
    
    
    public abstract void execute (Ctx ctx) throws Exception;


}
