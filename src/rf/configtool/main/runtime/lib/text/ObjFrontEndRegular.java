package rf.configtool.main.runtime.lib.text;

import java.util.List;

import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.lib.text.ObjText.FunctionLexer;
import rf.configtool.main.runtime.lib.text.ObjText.FunctionParser;

public class ObjFrontEndRegular extends Obj implements FrontEnd {
    
    public ObjFrontEndRegular (ObjLexerTokenStream tokenStream) {
//        this.add(new FunctionLexer());
//        this.add(new FunctionParserDynamic());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Parser.FrontEndRegular";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return getTypeName();
    }
    
    private Obj theObj () {
        return this;
    }
    
}
