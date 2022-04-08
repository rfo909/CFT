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
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.CodeLines;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Runtime;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;

/**
 * Lookup of identifier or call of function
 */
public class ExprLookupOrCall extends ExprCommon {

    private String ident;
    private List<Expr> params=new ArrayList<Expr>();
    
    public ExprLookupOrCall (TokenStream ts) throws Exception {
        super(ts);
        ident=ts.matchIdentifier("expected lookup identifier");
        if (ts.matchStr("(")) {
            boolean comma=false;
            while (!ts.peekStr(")")) {
                if (comma) {
                    ts.matchStr(",", "expected comma");
                }
                params.add(new Expr(ts));
                comma=true;
            }
            ts.matchStr(")", "expected ')' closing param list");
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        List<Value> values=new ArrayList<Value>();
        for (Expr e:params) values.add(e.resolve(ctx));
        
        // Global lookup
        Function f=ctx.getObjGlobal().getFunction(ident);
        if (f != null) return callFunction(f, ctx, values); 
        
        // variable lookup
        Value v=ctx.getVariable(ident);
        if (v != null) {
            if (values.size() > 0) throw new SourceException(getSourceLocation(), "variable lookup '" + ident + "' can not take params");
            return v;
        }
        
        // Code lookup
        ObjGlobal objGlobal=ctx.getObjGlobal();
        CodeLines codeLines=objGlobal.getCodeHistory().getNamedCodeLines(ident);
        if (codeLines!= null) {
            // execute code line
            Runtime rt=new Runtime(objGlobal);
        	CFTCallStackFrame caller=new CFTCallStackFrame(getSourceLocation(), "Calling " + ident);
            return rt.processCodeLines(ctx.getStdio(), caller, codeLines, new FunctionState(ident, values));
        }
        
        throw new SourceException(getSourceLocation(), "unknown symbol '" + ident + "'");
    }
    
    private Value callFunction(Function f, Ctx ctx, List<Value> values) throws Exception {
        try {
            return f.callFunction(ctx, values);
        } catch (Exception ex) {
            if (!(ex instanceof SourceException)) {
                throw new SourceException(getSourceLocation(), ex);
            } else {
                throw ex;
            }

        }
    }
}
