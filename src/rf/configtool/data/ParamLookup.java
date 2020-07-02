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
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;

public class ParamLookup extends LexicalElement {

    private Expr pos;
    private Expr defaultValue;
    
    public ParamLookup (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("P", "expected 'P'");
        if (ts.matchStr("(")) {
            pos=new Expr(ts);
            if (ts.matchStr(",")) {
                defaultValue=new Expr(ts);
            }
            ts.matchStr(")", "expected ')' closing P() expression");
        }
        
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        List<Value> params=ctx.getFunctionState().getParams();
        if (pos==null) return new ValueList(params);
        Value pV=pos.resolve(ctx);
        if (!(pV instanceof ValueInt)) {
            throw new Exception("position must be int");
        }
        int iPos=(int) ((ValueInt) pV).getVal();
        if (iPos < 1) throw new Exception("invalid position, must be 1 or greater");
        iPos--;
        
        Value def;
        if (defaultValue != null) def=defaultValue.resolve(ctx); else def=new ValueNull();
        
        if (iPos >= params.size()) {
            return def;
        } else {
            Value v=params.get(iPos);
            if (v instanceof ValueNull) return def;
            return v;
        }
    }
}
