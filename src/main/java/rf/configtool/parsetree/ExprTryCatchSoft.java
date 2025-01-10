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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.CodeException;
import rf.configtool.main.Ctx;
import rf.configtool.main.SoftErrorException;
import rf.configtool.main.SourceException;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDict;

/**
 * Try-catch for soft errors (created by error()) 
 */
public class ExprTryCatchSoft extends ExprCommon {
    
    private Expr expr;

    public ExprTryCatchSoft (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("tryCatchSoft","expected 'tryCatchSoft'");
        ts.matchStr("(", "expected '(' following tryCatchSoft");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing tryCatchSoft() expression");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        SoftErrorException softEx=null;
        
        Value result=null;

        CFTCallStackFrame top=ctx.getStdio().getTopCFTCallStackFrame();

        try {
            result = expr.resolve(ctx);
        } catch (Exception ex) {
            // examine if SoftException
            //
            if (ex instanceof SoftErrorException) {
                softEx=(SoftErrorException) ex;
            } else if (ex instanceof CodeException) {
                Exception inner=((CodeException) ex).getOriginalException();
                if (inner != null && (inner instanceof SoftErrorException)) {
                    softEx=(SoftErrorException) inner;
                }
            }
            if (softEx==null) throw ex;  
        }
        
        // found soft exception, returning dictionary
        
        ObjDict x=new ObjDict();
        x.set("ok", new ValueBoolean(result != null));
        if (result != null) x.set("result", result);
        if (softEx != null) x.set("msg", new ValueString(softEx.getMessage()));
        x.set("stack", ctx.getStdio().getCFTStackTrace(top));
  
        return new ValueObj(x);
    }
    
 
    
}
