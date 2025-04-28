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

package rf.xlang.parsetree;

import java.util.*;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;

public class Expr extends ExprCommon {
    
    protected static String matchSeparator (TokenStream ts, String[] sep) {
        for (String s:sep) {
            try {
                if (ts.matchStr(s)) return s;
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
    
    private String[] sep= {"||"};
    private List<ExprA> parts=new ArrayList<ExprA>();
    private List<String> separators=new ArrayList<String>();
    
    public Expr (TokenStream ts) throws Exception {
        super(ts);
        for (;;) {
            parts.add(new ExprA(ts));
            String x=Expr.matchSeparator(ts,sep);
            if (x != null) {
                separators.add(x);
            } else {
                break;
            }
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        try {
            if (parts.size() == 1) {
                return parts.get(0).resolve(ctx);
            }
    
            // logical or, implement short-cut processing
            for (ExprA part:parts) {
                Value v=part.resolve(ctx);
                if (!(v instanceof ValueBoolean)) {
                    throw new SourceException(getSourceLocation(), "expected boolean value");
                }
                if ( ((ValueBoolean) v).getVal()) {
                    return v;
                }
            }
            
            return new ValueBoolean(false);
        } catch (Exception ex) {
            throw new SourceException(getSourceLocation(), ex);
        }
    }
}
