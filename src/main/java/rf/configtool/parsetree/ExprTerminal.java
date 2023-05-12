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

import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.CustomException;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueString;

public class ExprTerminal extends ExprCommon {
    
    private ExprCommon expr;
    private Value literalValue;
    private ExprE notExpr;
    private Expr negExpr;

    public ExprTerminal (TokenStream ts) throws Exception {
        super(ts);
        
        
        if (ts.peekStr("as")) {
            expr=new ExprAs(ts);
            return;
        }
        
        if (ts.peekType(Token.TOK_IDENTIFIER) && ts.peekStr(1,"=")) {
            expr = new ExprAssign(ts);
            return;
        }
        
        
        if (ts.matchStr("(")) {
            expr=new Expr(ts);
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
            expr=new ExprPop(ts);
            return;
        }
        if (ts.peekStr("if")) {
            expr=new ExprIf(ts);
            return;
        }
        
        if (ts.matchStr("null")) {
            literalValue=new ValueNull();
            return;
        }
        if (ts.peekStr(1,":")) {
            expr=new ExprScriptCall(ts);
            return;
        }
        if (ts.peekStr("::") || (ts.peekStr(":") && ts.peekType(1,Token.TOK_INT))) {
        	expr=new ExprLastResult(ts);
        	return;
        }
        if (ts.peekStr("tryCatch")) {
            expr=new ExprTryCatch(ts);
            return;
        }
        if (ts.peekStr("tryCatchSoft")) {
            expr=new ExprTryCatchSoft(ts);
            return;
        }
        if (ts.peekStr("Sequence")) {
            expr = new ExprSequence(false,ts);
            return;
        }
        if (ts.peekStr("CondSequence")) {
            expr = new ExprSequence(true,ts);
            return;
        }
        if (ts.peekStr("SpawnProcess")) {
            expr = new ExprSpawnProcess(ts);
            return;
        }
        if (ts.peekStr("&")) {
            expr=new ExprAmp(ts);
            return;
        }
        if (ts.peekStr("SymDict")) {
            expr=new ExprSymDict(ts);
            return;
        }
        if (ts.peekStr("%%")) {
            expr=new ExprSymGetSet(ts,true);
            return;
        }
        if (ts.peekStr("%")) {
            expr=new ExprSymGetSet(ts,false);
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
            expr = new ExprBlock(ts);
            return;
        }
        if (ts.peekStr("P")) {
            expr=new ExprParamLookup(ts);
            return;
        }
        if (ts.peekStr("PDict")) {
            expr=new ExprParamLookupDict(ts);
            return;
        }
        expr=new ExprLookupOrCall(ts);
        
    }
    
    public Value resolve (Ctx ctx) throws Exception {
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
            if (ex instanceof CustomException) {
                throw ex;
            } else {
                throw new SourceException(getSourceLocation(), ex);
            }

        }
    }

}
