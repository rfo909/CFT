package rf.configtool.data;

import rf.configtool.parser.*;

public class LexicalElement {

    private SourceLocation sourceLocation;
    
    public LexicalElement (TokenStream ts) throws Exception {
        sourceLocation=ts.getSourceLocation();
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }
    
    
}
