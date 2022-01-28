/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;

public class StmtReportList extends Stmt {

    private Expr listValue;
    
    public StmtReportList (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("reportList","expected 'reportList'");
        ts.matchStr("(", "expected '(' following reportList");
        
        listValue=new Expr(ts);
        ts.matchStr(")", "expected ')' closing reportList() statement");
    }

    public void execute (Ctx ctx) throws Exception {
        
        Value v=listValue.resolve(ctx);
        if (!(v instanceof ValueList)) throw new Exception("Expected parameter for reportList() to be list");

        List<Value> result=((ValueList) v).getVal();

        ctx.getOutText().addReportData(result);
    }

}
