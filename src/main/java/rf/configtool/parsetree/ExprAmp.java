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

import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjClosure;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjProcess;

/**
 * Compact and simplified version of SpawnProcess, meant for interactive use, where there are no
 * variables.
 */
public class ExprAmp extends ExprCommon {
    
    private static int counter=0;

    private Expr expr;
    
    private String name;
    private Expr nameExpr;

    private String getName(String text) {
        String s="(" + (counter++) + ")";
        if (text != null) {
            s=s+" " + text;
        }
        return s;
    }
    
    
    public ExprAmp(TokenStream ts) throws Exception {
        super(ts);

        ts.matchStr("&", "expected symbol '&'");
        expr = new Expr(ts);
        if (ts.matchStr(",")) {
            if (ts.peekType(Token.TOK_IDENTIFIER)) {
                name=ts.matchIdentifier();
            } else {
                nameExpr=new Expr(ts);
            }
        }
    }
    

    public Value resolve (Ctx ctx) throws Exception {
        ObjDict dict=new ObjDict();
        ObjClosure closure=null;
        
        ObjProcess process = new ObjProcess(dict, expr, closure);
        process.start(ctx);
        
        String text=name;
        if (text==null && nameExpr != null) {
            Value v=nameExpr.resolve(ctx);
            text=v.getValAsString();
            int maxLength=ctx.getObjGlobal().getRoot().getObjTerm().getScreenWidth()/2;
            
            if (text.length() > maxLength) {
                text=text.substring(maxLength-4) + " ...";
            }
        }
        
        // add to Jobs object
        String pid=getName(text);
        ctx.getObjGlobal().getRoot().getBackgroundProcesses().add(pid, process);
        return new ValueString(pid);
        
    }
}
