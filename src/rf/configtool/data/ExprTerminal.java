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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;

public class ExprTerminal extends LexicalElement {
    
    private Expr innerExpr;
    private ExprE notExpr;
    private Expr negExpr;
    private ExprPop pop;
    private ExprIf exprIf;
    private ExprPwd exprPwd;
    private boolean nullValue;
    private ExprCall exprCall;
    private ExprTryUnsafe exprTryUnsafe;
    
    private Value literalValue;
    private ParamLookup paramLookup;
    private ParamLookupDict paramLookupDict;
    private ExprBlock exprMacro;
    private LookupOrCall lookupCall; // lookup or call
    
    public ExprTerminal (TokenStream ts) throws Exception {
        super(ts);
        
        if (ts.matchStr("(")) {
            innerExpr=new Expr(ts);
            ts.matchStr(")", "expected ')'");
            return;
        }

        if (ts.matchStr("!")) {
            notExpr=new ExprE(ts);
            return;
        }
        if (ts.matchStr("-")) {
            negExpr=new Expr(ts);
            return;
        }
        if (ts.peekStr("_")) {
            pop=new ExprPop(ts);
            return;
        }
        if (ts.peekStr("if") || ts.peekStr("when")) {
            exprIf=new ExprIf(ts);
            return;
        }
        if (ts.peekStr("pwd")) {
            exprPwd=new ExprPwd(ts);
            return;
        }
        if (ts.matchStr("null")) {
            nullValue=true;
            return;
        }
        if (ts.peekStr("call")) {
            exprCall=new ExprCall(ts);
            return;
        }
        if (ts.peekStr("tryUnsafe")) {
            exprTryUnsafe=new ExprTryUnsafe(ts);
            return;
        }
        
        if (ts.peekType(Token.TOK_INT)) {
            literalValue=new ValueInt(ts.matchInt("expected integer literal"));
            return;
        }
        if (ts.peekType(Token.TOK_STRING)) {
            literalValue=new ValueString(ts.matchType(Token.TOK_STRING, "expected string literal").getStr());
            return;
        }
        if (ts.peekType(Token.TOK_FLOAT)) {
            literalValue=new ValueFloat(ts.matchFloat("expected float literal"));
            return;
        }
        if (ts.matchStr("true")) {
            literalValue=new ValueBoolean(true);
            return;
        }
        if (ts.matchStr("false")) {
            literalValue=new ValueBoolean(false);
            return;
        }
        if (ts.peekStr("Inner") || ts.peekStr("Lambda") || ts.peekStr("{")) {
            exprMacro = new ExprBlock(ts);
            return;
        }
        if (ts.peekStr("P")) {
            paramLookup=new ParamLookup(ts);
            return;
        }
        if (ts.peekStr("PDict")) {
            paramLookupDict=new ParamLookupDict(ts);
            return;
        }
        lookupCall=new LookupOrCall(ts);
        
    }
    
    public Value resolve (Ctx ctx) throws Exception {
    	try {
	        if (innerExpr != null) return innerExpr.resolve(ctx);
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
	        if (pop != null) return pop.resolve(ctx);
	        if (exprIf != null) return exprIf.resolve(ctx);
	        if (exprPwd != null) return exprPwd.resolve(ctx);
	        if (nullValue) return new ValueNull();
	        if (exprCall != null) return exprCall.resolve(ctx);
	        if (exprTryUnsafe != null) return exprTryUnsafe.resolve(ctx);
	        
	        if (literalValue != null) return literalValue;
	        if (paramLookup != null) return paramLookup.resolve(ctx);
	        if (paramLookupDict != null) return paramLookupDict.resolve(ctx);
	        if (exprMacro != null) return exprMacro.resolve(ctx);
	        if (lookupCall != null) return lookupCall.resolve(ctx);
	        
	        throw new RuntimeException("Internal error");
    	} catch (Exception ex) {
    		if (ex instanceof SourceException) {
    			throw ex;
    		} else {
    			throw new SourceException(getSourceLocation(), ex);
    		}

    	}
    }

}
