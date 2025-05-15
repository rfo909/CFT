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
import rf.xlang.main.runtime.*;

public class StmtFor extends Stmt {

    private List<Stmt> body=new ArrayList<>();

    private String loopVariable;
    private Expr listExpr;

    public StmtFor(TokenStream ts) throws Exception {
        super(ts);

        ts.matchStr("for","expected keyword 'for'");
        ts.matchStr("(", "expected '(' following keyword 'for'");
        loopVariable = ts.matchIdentifier("expected loop variable");
        ts.matchStr(":", "expected ':' between loop variable and list expression");
        listExpr=new Expr(ts);
        ts.matchStr(")", "expected ')'");

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
        List<Value> data;

        Value x=listExpr.resolve(ctx);
        if (x instanceof ValueList) {
            data = ((ValueList) x).getVal();
        } else {
            List<Value> xx=new ArrayList<Value>();
            xx.add(x);
            data=xx;
        }

        OUTER: for(int pos=0; pos < data.size(); pos++) {
            Value iterValue = data.get(pos);

            Ctx sub=ctx.sub();
            sub.setLoopVariable(loopVariable, iterValue);

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
