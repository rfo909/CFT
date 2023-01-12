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
import rf.configtool.main.runtime.Value;

public class StmtAssertRejectContinue extends Stmt{
    
	static final int TYPE_ASSERT = 0;
	static final int TYPE_REJECT = 1;
	static final int TYPE_CONTINUE = 2;
	
	private int type;
    private Expr expr;

    public StmtAssertRejectContinue (TokenStream ts) throws Exception {
        super(ts);
        
        if (ts.matchStr("assert")) {
        	type = TYPE_ASSERT;
        } else if (ts.matchStr("reject")) {
        	type = TYPE_REJECT;
        } else if (ts.matchStr("continue")) {
        	type = TYPE_CONTINUE;
        } else {
        	throw new Exception("Invalid token");
        }

        if (type==TYPE_ASSERT || type==TYPE_REJECT) {
	        ts.matchStr("(", "expected '(' following assert/reject");
	        expr=new Expr(ts);
	        ts.matchStr(")", "expected ')' closing assert/reject statement");
        }
    }
    
    public void execute (Ctx ctx) throws Exception {
    	if (type==TYPE_CONTINUE) {
    		ctx.setAbortIterationFlag();
    		return;
    	}
    	
        Value v=expr.resolve(ctx);
        boolean x=v.getValAsBoolean();
        
        boolean abortProcessing=(type==TYPE_ASSERT && !x) || (type==TYPE_REJECT && x);
        if (abortProcessing) {
            ctx.setAbortIterationFlag();
        }
    }


}
