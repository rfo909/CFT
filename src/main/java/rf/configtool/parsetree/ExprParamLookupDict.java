/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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
import rf.configtool.main.runtime.lib.ObjDict;

public class ExprParamLookupDict extends ExprCommon {

    private List<Expr> names=new ArrayList<Expr>();
    
    public ExprParamLookupDict (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("PDict", "expected 'PDict'");
        
        
        boolean comma=false;
        
        
        ts.matchStr("(","expected '('");
        for (;;) {
            if (ts.matchStr(")")) break;
            if (comma) ts.matchStr(",", "expected comma");
            names.add(new Expr(ts));
            comma=true;
        }
        if (names.size()==0) throw new Exception("Expected at least one field name");
        
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        List<Value> params=ctx.getFunctionState().getParams();
        Map<String,Value> map=new HashMap<String,Value>();
        ObjDict x=new ObjDict();
        
        for (int i=0; i<names.size(); i++) {
            String name=names.get(i).resolve(ctx).getValAsString();
            Value value;
            if (i < params.size()) {
                value=params.get(i);
            } else {
                value=new ValueNull();
            }
            x.set(name, value);
        }
        return new ValueObj(x);
    }
}
