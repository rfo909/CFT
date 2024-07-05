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

import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;

public class ExprE extends ExprCommon {
    
    private ExprTerminal firstPart;
    private List<DottedCall> dottedLookups=new ArrayList<DottedCall>();
    private DottedAssign dottedAssign;
    
    public ExprE (TokenStream ts) throws Exception {
        super(ts);
        firstPart=new ExprTerminal(ts);
        for (;;) {
            
            if (ts.peekStr(".") && ts.peekType(1,Token.TOK_IDENTIFIER) && ts.peekStr(2,"=")) {
                dottedAssign=new DottedAssign(ts);
                break; // terminates sequence
            }
            
            if (!ts.peekStr(".")) break;
            //
            dottedLookups.add(new DottedCall(ts));
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        Value v=firstPart.resolve(ctx);
        
        for (DottedCall dottedCall:dottedLookups) {
            v=dottedCall.resolve(ctx, v);
        }
        if (dottedAssign != null) {
            v=dottedAssign.resolve(ctx,v);
        }
        return v;
    }


}
