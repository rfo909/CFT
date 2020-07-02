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
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;

public class ExprWhen extends LexicalElement {

    private Expr bool, expr;
    
    public ExprWhen (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("when", "expected 'when'");
        ts.matchStr("(", "expected '(' following keyword 'when'");
        bool=new Expr(ts);
        ts.matchStr(",", "expected comma following boolean expr");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing when expression");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        boolean b=bool.resolve(ctx).getValAsBoolean();
        if (b) {
            Value val=expr.resolve(ctx);
            return val;
        } else {
            return new ValueNull();
        }
    }
}
