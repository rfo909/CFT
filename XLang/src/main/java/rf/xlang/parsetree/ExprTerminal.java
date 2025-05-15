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

import rf.xlang.lexer.Token;
import rf.xlang.lexer.TokenStream;
import rf.xlang.main.Ctx;
import rf.xlang.main.SourceException;
import rf.xlang.main.runtime.Value;
import rf.xlang.main.runtime.ValueBoolean;
import rf.xlang.main.runtime.ValueFloat;
import rf.xlang.main.runtime.ValueInt;
import rf.xlang.main.runtime.ValueNull;
import rf.xlang.main.runtime.ValueString;

public class ExprTerminal extends ExprCommon {
    
    private ExprCommon expr;
    private Value literalValue;
    private Expr notExpr;
    private Expr negExpr;
    
    private ExprLookupOrCall next; // an ExprTerminal followed by .something

    public ExprTerminal (TokenStream ts) throws Exception {
        super(ts);
        
        
        if (ts.peekType(Token.TOK_IDENTIFIER) && ts.peekStr(1,"=")) {
            expr = new ExprAssign(ts);
        } else if (ts.matchStr("(")) {
            expr=new Expr(ts);
            ts.matchStr(")", "expected ')'");
        } else if (ts.matchStr("!")) {
            notExpr=new Expr(ts);
        } else if (ts.matchStr("-")) {
            negExpr=new Expr(ts);
        } else if (ts.matchStr("null")) {
            literalValue=new ValueNull();
        } else if (ts.peekType(Token.TOK_INT)) {
            literalValue=new ValueInt(ts.matchInt("expected integer literal"));
        } else if (ts.peekType(Token.TOK_STRING)) {
            literalValue=new ValueString(ts.matchType(Token.TOK_STRING, "expected string literal").getStr());
        } else if (ts.peekType(Token.TOK_FLOAT)) {
            literalValue=new ValueFloat(ts.matchFloat("expected float literal"));
        } else if (ts.matchStr("true")) {
            literalValue=new ValueBoolean(true);
        } else if (ts.matchStr("false")) {
            literalValue=new ValueBoolean(false);
        } else {
            expr=new ExprLookupOrCall(ts);
        }
        
        if (ts.matchStr(".")) {
            // will not happen for ExprLookupOrCall above, as it consumes all dot-lookups
            next=new ExprLookupOrCall(ts);
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        Value v=firstResolve(ctx);
        if (next != null) {
            return next.resolve(ctx,v);
        } else {
            return v;
        }
    }
    
    public Value firstResolve (Ctx ctx) throws Exception {
        try {
            if (expr != null) return expr.resolve(ctx);

            if (literalValue != null) return literalValue;
            

            if (notExpr != null) {
                Value v=notExpr.resolve(ctx);
                return new ValueBoolean(!v.getValAsBoolean());
            }
            if (negExpr != null) {
                Value v=negExpr.resolve(ctx);
                if (v instanceof ValueInt) {
                    long result=-((ValueInt)v).getVal();
                    return new ValueInt(result);
                } else if (v instanceof ValueFloat) {
                    double result=-((ValueFloat)v).getVal();
                    return new ValueFloat(result);
                } else {
                    throw new SourceException(negExpr.getSourceLocation(), "expected numeric value (int/float)");
                }
            }

            throw new RuntimeException("Internal error");
        } catch (Exception ex) {
            throw new SourceException(getSourceLocation(), ex);
        }
    }

}
