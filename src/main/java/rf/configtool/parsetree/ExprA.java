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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;

public class ExprA extends ExprCommon {
    private String[] sep= {"&&"};

    private List<ExprB> parts=new ArrayList<ExprB>();
    private List<String> separators=new ArrayList<String>();
    
    public ExprA (TokenStream ts) throws Exception {
        super(ts);
        for (;;) {
            parts.add(new ExprB(ts));
            String x=Expr.matchSeparator(ts,sep);
            if (x != null) {
                separators.add(x);
            } else {
                break;
            }
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        if (parts.size() == 1) {
            return parts.get(0).resolve(ctx);
        }
        
        // logical and, implement short-cut processing
        for (ExprB part:parts) {
            Value v=part.resolve(ctx);
            if (!(v instanceof ValueBoolean)) {
                throw new SourceException(getSourceLocation(), "expected boolean value");
            }
            if ( ! ((ValueBoolean) v).getVal()) {
                return new ValueBoolean(false);
            }
        }
        
        return new ValueBoolean(true);
    }

}
