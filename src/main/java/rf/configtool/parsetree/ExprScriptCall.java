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

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;
import rf.configtool.root.ScriptState;

/**
 * Return directory object for current directory
 */
public class ExprScriptCall extends ExprCommon {

    private String script;
    private String func;
    private List<Expr> params;
    
    // call "savefile:name" with Data (...)
    
    public ExprScriptCall (TokenStream ts) throws Exception {
        super(ts);
        script=ts.matchIdentifier("expected script name (identifier)");
        ts.matchStr(":", "expected ':'");
        func=ts.matchIdentifier("expected function name following '" + script + ":'");

        params=new ArrayList<Expr>();
        if (ts.matchStr("(")) {
            boolean comma=false;
            while (!ts.matchStr(")")) {
                if(comma) ts.matchStr(",", "expected comma");
                params.add(new Expr(ts));
                comma=true;
            }
        }

    }
    
      public Value resolve (Ctx ctx) throws Exception {

        List<Value> args=new ArrayList<Value>();
        for (Expr expr:params) args.add(expr.resolve(ctx));
        
        ObjGlobal objGlobal=ctx.getObjGlobal();
        Stdio stdio=ctx.getStdio();
        
        //return objGlobal.getRoot().invokeScriptFunction(script, func, args);
        ScriptState x=objGlobal.getRoot().getScriptState(script, false);
        CFTCallStackFrame caller=new CFTCallStackFrame(getSourceLocation(), "Calling " + script + ":" + func);
        Value retVal=x.invokeFunction (stdio, caller, func, args);
        return retVal;
    }
}
