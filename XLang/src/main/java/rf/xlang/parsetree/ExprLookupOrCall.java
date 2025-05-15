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

import java.util.ArrayList;
import java.util.List;

import rf.xlang.lexer.TokenStream;
import rf.xlang.main.Ctx;
import rf.xlang.main.ObjGlobal;
import rf.xlang.main.runtime.*;
import rf.xlang.main.runtime.lib.ObjTuple;
/**
 * Lookup of identifier or call of function
 */
public class ExprLookupOrCall extends ExprCommon {

    private String ident;
    private List<Expr> params=new ArrayList<Expr>();
    private ExprLookupOrCall next;
    
    public ExprLookupOrCall (TokenStream ts) throws Exception {
        super(ts);
        ident=ts.matchIdentifier("expected lookup identifier");
        if (ts.matchStr("(")) {
            while (!ts.peekStr(")")) {
                params.add(new Expr(ts));
                if (!ts.matchStr(",")) break;
            }
            ts.matchStr(")", "expected ')' closing param list");
        }
        if (ts.matchStr(".")) {
            next=new ExprLookupOrCall(ts);
        }
    }
    
    @Override
    public Value resolve (Ctx ctx) throws Exception {
        List<Value> parameters = new ArrayList<Value>();
        for (Expr expr : params) {
            parameters.add(expr.resolve(ctx));
        }

        ObjGlobal objGlobal=ctx.getObjGlobal();

        boolean found=false;
        Value currValue=null;

        if (!found) {
            if (ctx.checkLoopVariable(ident)) {
                currValue=ctx.getLoopVariableValue();
                found=true;
            }
        }

        if (!found) {
            currValue = ctx.getVariable(ident);
            if (currValue != null) {
                found=true;
                //System.out.println("ExprLookupOrCall: local variable " + ident);
            }
        }

        if (!found) {
            Function memberFunction = objGlobal.getFunction(ident);
            if (memberFunction != null) {
                currValue = memberFunction.callFunction(ctx, parameters);
                found=true;
                //System.out.println("ExprLookupOrCall: calling global function " + ident);

            }
        }
        if (!found) {
            CodeFunction codeFunction = objGlobal.getCodeFunction(ident);
            if (codeFunction != null) {
                currValue = codeFunction.execute(ctx, parameters);
                found = true;
                //System.out.println("ExprLookupOrCall: calling script function " + ident);

            }
        }
        if (!found) {
            TupleType type = objGlobal.getTupleType(ident);
            if (type != null) {
                currValue = new ValueObj(new ObjTuple(type, parameters));
                found=true;
                //System.out.println("ExprLookupOrCall: creating tuple " + ident);

            }
        }
        if (!found) {
            throw ex("Invalid lookup: " + ident);
        }

        if (currValue==null) {
            throw ex("Internal error");
        }

        if (next != null) {
            return next.resolve(ctx, currValue);
        } else {
            return currValue;
        }
    }

    public Value resolve (Ctx ctx, Value value) throws Exception {
        if (value instanceof ValueNull) {
            throw ex("Null-pointer, can not resolve ." + ident);
        }


        List<Value> parameters = new ArrayList<Value>();
        for (Expr expr : params) {
            parameters.add(expr.resolve(ctx));
        }

        Obj obj;

        if (value instanceof ValueObj) {
            obj = ((ValueObj) value).getVal();
        } else {
            obj = value;
        }


        Value currValue=null;

        Function f = obj.getFunction(ident);
        if (f == null) {
            // specials: Tuple lookup
            if (parameters.size() == 0) {
                if (obj instanceof ObjTuple) {
                    currValue=((ObjTuple) obj).getValue(ident);
                    //if (currValue != null) System.out.println("ExprLookupOrCall: tuple lookup ok " + ident + " -> " + currValue.getValAsString());
                }
            }

            if (currValue==null) throw ex("Unknown dotted function: " + ident);
        } else {
            currValue = f.callFunction(ctx, parameters);
        }
        if (next != null) {
            return next.resolve(ctx,currValue);
        } else {
            return currValue;
        }
    }

}
