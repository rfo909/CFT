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

import java.util.ArrayList;
import java.util.List;

import rf.xlang.lexer.TokenStream;
import rf.xlang.main.Ctx;

public class StmtLoop extends Stmt {
    
    private List<Stmt> body=new ArrayList<>();
    
    public StmtLoop (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("loop","expected keyword 'loop'");
        if (ts.matchStr("{")) {
            while (!ts.matchStr("}")) body.add(Stmt.parse(ts));
        } else {
            body.add(Stmt.parse(ts));
        }
        
    }

    /**
     * Loops execute in sub-contexts maintaining control over break and continue
     * Note that if return is called inside a loop, we must transfer the return value
     * from the sub-context to ctx then return
     */
    public void execute (Ctx ctx) throws Exception {
        OUTER: for (;;) {
            Ctx sub=ctx.sub();

            for (Stmt stmt:body) {
                stmt.execute(sub);
                if (sub.hasBreakLoopFlag()) {
                    break OUTER;
                }
                if (sub.hasContinueIterationFlag()) {
                    continue OUTER;
                }
                // transfer return value from sub-context
                if (sub.getFunctionReturnValue() != null) {
                    ctx.setFunctionReturnValue(sub.getFunctionReturnValue());
                    return;
                }
             }

        }
    }

    

}
