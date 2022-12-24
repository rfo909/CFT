/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDict;

/**
 * Try-catch for all errors (hard and soft). For soft only, use tryCatchSoft()
 */
public class ExprTryCatch extends ExprCommon {
    
    private Expr expr;

    public ExprTryCatch (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("tryCatch","expected 'tryCatch'");
        ts.matchStr("(", "expected '(' following tryCatch");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing tryCatch() expression");
    }
    
    
    
    public Value resolve (Ctx ctx) throws Exception {
        Value result=null;
        ValueList cftStackTrace=null;
        ValueList javaStackTrace=null;
        Value msg=null;
        
        CFTCallStackFrame top=ctx.getStdio().getTopCFTCallStackFrame();
        
        try {
            result = expr.resolve(ctx);
        } catch (Exception ex) {
            cftStackTrace=ctx.getStdio().getCFTStackTrace(top);
            javaStackTrace=getStackTrace(ex);
            msg=new ValueString(ex.getMessage());
        }
        ObjDict x=new ObjDict();
        x.set("ok", new ValueBoolean(result != null));
        if (result != null) x.set("result", result);
        if (cftStackTrace != null) x.set("stack", cftStackTrace);
        if (javaStackTrace != null) x.set("javastack", javaStackTrace);
        if (msg != null) x.set("msg", msg);
        
        return new ValueObj(x);
    }
    
    private ValueList getStackTrace(Exception ex) {
        List<Value> data=new ArrayList<Value>();
        if (ex instanceof SourceException) {
            Exception x=((SourceException) ex).getOriginalException();
            if (x != null) ex=x;
        }
        StackTraceElement[] st = ex.getStackTrace();
        for (StackTraceElement e:st) {
            data.add(new ValueString(e.toString()));
        }
        return new ValueList(data);
    }
}
