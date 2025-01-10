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

package rf.configtool.parsetree;

import java.util.*;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjRow;
import rf.configtool.main.runtime.ValueList;

public class StmtReport extends Stmt {

    private List<Expr> presentationValues=new ArrayList<Expr>();
    
    public StmtReport (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("report","expected 'report'");
        ts.matchStr("(", "expected '(' following report");
        
        //boolean comma=false;
        boolean first=true;
        while (!ts.matchStr(")")) {
            if (!first) {
                ts.matchStr(",", "expected comma separating values, or ')' closing arglist");
            }
            presentationValues.add(new Expr(ts));
            first=false;
        }
    }

    public void execute (Ctx ctx) throws Exception {
        List<Value> data=new ArrayList<Value>();
        
        for (Expr expr:presentationValues) {
            data.add(expr.resolve(ctx));
        }
        
        if (data.size()==1 && (data.get(0) instanceof ValueList)) {
            data=((ValueList) data.get(0)).getVal();
        }

        ctx.getOutData().out(new ValueObj(new ObjRow(data)));
    }

}
