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

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDict;

/**
 * <Dict>.x=Expr
 */
public class DottedAssign extends LexicalElement {

    private String ident;
    private Expr expr;
    
    public DottedAssign (TokenStream ts) throws Exception {
        super(ts);

        ts.matchStr(".","Expected .identifier=Expr");
        ident=ts.matchIdentifier("expected .identifier");
        ts.matchStr("=", "Expected .identifier=Expr");
        expr=new Expr(ts);
    }
    
    
    
    public Value resolve (Ctx ctx, Obj obj) throws Exception {
        if (obj instanceof ValueObj) {
            // unwrap Obj
            obj=((ValueObj) obj).getVal();
        }
        
        if (obj instanceof ObjDict) {
            ObjDict dict=(ObjDict) obj;
            
            Value v=expr.resolve(ctx.sub());
            dict.set(ident, v);
            return new ValueObj(dict);
        } else {
            throw new Exception("Expected a dictionary to set '" + ident + "' - got " + obj.getTypeName());
        }
    }

}
