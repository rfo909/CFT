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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.parser.TokenStream;

public class StmtReport extends Stmt {

    private List<Expr> values=new ArrayList<Expr>();
    
    public StmtReport (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("report","expected 'report'");
        ts.matchStr("(", "expected '(' following report");
        boolean comma=false;
        while (!ts.matchStr(")")) {
            if (comma) ts.matchStr(",", "expected comma separating values, or ')' closing arglist");
            values.add(new Expr(ts));
            comma=true;
        }
    }

    public void execute (Ctx ctx) throws Exception {
        List<Value> result=new ArrayList<Value>();
        for (Expr expr:values) {
            result.add(expr.resolve(ctx));
        }
        if (result.size()==1 && (result.get(0) instanceof ValueList)) {
        	result=((ValueList) result.get(0)).getVal();
        }

        ctx.getOutText().addReportData(result);
    }

}
