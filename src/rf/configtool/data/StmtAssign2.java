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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class StmtAssign2 extends Stmt {

    private String varName;
    private Expr expr;
    
    public StmtAssign2 (TokenStream ts) throws Exception {
        super(ts);
        varName=ts.matchIdentifier("expected variable name");
        ts.matchStr("=","expected '='");
        expr=new Expr(ts);
       
    }

    public void execute (Ctx ctx) throws Exception {
        Value v=expr.resolve(ctx);
        ctx.getFunctionState().set(varName, v);
    }

}