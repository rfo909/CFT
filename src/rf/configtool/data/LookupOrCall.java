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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.CodeLines;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;

/**
 * Lookup of identifier or call of function
 */
public class LookupOrCall extends LexicalElement {

    private String ident;
    private List<Expr> params=new ArrayList<Expr>();
    
    public LookupOrCall (TokenStream ts) throws Exception {
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
        
//        if (dotValue != null) {
//            Obj obj=dotValue;
//            if (obj instanceof ValueObj) {
//                obj=((ValueObj) obj).getVal();
//            }
//            Function f=obj.getFunction(ident);
//            if (f==null) throw new Exception(getSourceLocation() + " " + obj.getDescription() + " no method '" + ident + "'");
//
//            return callFunction(f, ctx, values);
//        }
        
        
        // Global lookup
        Function f=ctx.getObjGlobal().getFunction(ident);
        if (f != null) return callFunction(f, ctx, values); 
        
        // variable lookup
        Value v=ctx.getVariable(ident);
        if (v != null) {
            if (values.size() > 0) throw new Exception(getSourceLocation() + " variable lookup '" + ident + "' can not take params");
            return v;
        }
        
        // Code lookup
        ObjGlobal objGlobal=ctx.getObjGlobal();
        CodeLines codeLines=objGlobal.getCodeHistory().getNamedCodeLines(ident);
        if (codeLines!= null) {
            //if (values.size() > 0) throw new Exception(getSourceLocation() + " code identified by '" + ident + "' can not take params");
            // execute code line
            Runtime rt=new Runtime(objGlobal);
            return rt.processCodeLines(codeLines, new FunctionState(values));
        }
        
        throw new Exception(getSourceLocation() + " unknown symbol '" + ident + "'");
    }
    
    private Value callFunction(Function f, Ctx ctx, List<Value> values) throws Exception {
        return f.callFunction(ctx, values);
    }
}
