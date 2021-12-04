package rf.configtool.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;

public abstract class ExprCommon extends LexicalElement  {
	
    public ExprCommon (TokenStream ts) throws Exception {
        super(ts);
    }

    public abstract Value resolve (Ctx ctx) throws Exception;

}
