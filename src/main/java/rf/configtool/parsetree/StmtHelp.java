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

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.ValueObj;

public class StmtHelp extends Stmt {
    
    private Expr expr;
    
    public StmtHelp (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("help", "expected 'help' keyword");
        if (ts.matchStr("(")) {
            expr=new Expr(ts);
            ts.matchStr(")", "expected ')' closing help()");
        }
    }
    
    public void execute (Ctx ctx) throws Exception {
        Obj v;
        if (expr != null) {
            v=expr.resolve(ctx);
            if (v instanceof ValueObj) v=((ValueObj)v).getVal();
        } else {
            v=ctx.pop();
            if (v==null) {
                v=ctx.getObjGlobal();
            } else if (v instanceof ValueObj) {
                v=((ValueObj)v).getVal();
            }
        }
        
        v.generateHelp(ctx);
        ctx.push(new ValueObj(v));
    }


}
