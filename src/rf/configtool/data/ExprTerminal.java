package rf.configtool.data;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;

public class ExprTerminal extends LexicalElement {
    
    private Expr innerExpr;
    private Expr notExpr;
    private Expr negExpr;
    private ExprPop pop;
    private ExprIf exprIf;
    private ExprWhen exprWhen;
    private ExprLs exprDir;
    private ExprPwd exprPwd;
    private boolean nullValue;
    private ExprCall exprCall;
    
    private Value literalValue;
    private ParamLookup paramLookup;
    private ParamLookupDict paramLookupDict;
    private ExprMacro exprMacro;
    private LookupOrCall lookupCall; // lookup or call
    
    public ExprTerminal (TokenStream ts) throws Exception {
        super(ts);
        
        if (ts.matchStr("(")) {
            innerExpr=new Expr(ts);
            ts.matchStr(")", "expected ')'");
            return;
        }

        if (ts.matchStr("!")) {
            notExpr=new Expr(ts);
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
        if (ts.peekStr("if")) {
            exprIf=new ExprIf(ts);
            return;
        }
        if (ts.peekStr("when")) {
        	exprWhen=new ExprWhen(ts);
        	return;
        }
        if (ts.peekStr("ls") || ts.peekStr("lsf") || ts.peekStr("lsd")) {
            exprDir=new ExprLs(ts);
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
        if (ts.peekStr("{")) {
        	exprMacro = new ExprMacro(ts);
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
                throw new Exception(negExpr.getSourceLocation() + " expected numeric value (int/float)");
            }
        }
        if (pop != null) return pop.resolve(ctx);
        if (exprIf != null) return exprIf.resolve(ctx);
        if (exprWhen != null) return exprWhen.resolve(ctx);
        if (exprDir != null) return exprDir.resolve(ctx);
        if (exprPwd != null) return exprPwd.resolve(ctx);
        if (nullValue) return new ValueNull();
        if (exprCall != null) return exprCall.resolve(ctx);
        
        if (literalValue != null) return literalValue;
        if (paramLookup != null) return paramLookup.resolve(ctx);
        if (paramLookupDict != null) return paramLookupDict.resolve(ctx);
        if (exprMacro != null) return exprMacro.resolve(ctx);
        if (lookupCall != null) return lookupCall.resolve(ctx);
        
        throw new RuntimeException("Internal error");
    }

}
