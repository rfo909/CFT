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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueString;

public class ExprLastResult extends ExprCommon {

    private final Long pos;
    
    public ExprLastResult (TokenStream ts) throws Exception {
        super(ts);
        if (ts.matchStr("::")) {
            pos=null;
        } else {
            ts.matchStr(":","ExprLastResult: expected '::' or ':N'");
            pos=ts.matchInt("ExprLastResult: expected int following ':'");
        }
    }
    
    
    public Value resolve (Ctx ctx) throws Exception {
        Value v=ctx.getObjGlobal().getRoot().getLastResult();
        if (pos==null) return v;
        if (!(v instanceof ValueList)) throw new Exception("LastResult not list");
        List<Value> list=((ValueList) v).getVal();
        return list.get(pos.intValue());
        
    }
}
