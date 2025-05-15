package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.xlang.lexer.TokenStream;

/**
 * A Tuple is a List with named content ... or a Dict
 *
 * To create, call like a function
 * type FileLine (file, line, lineNumber)
 *
 * x = FileLine(someFile, "text", 23)
 * x.someFile = y
 *
 */
public class TupleType extends LexicalElement {
    
    private String typeName;
    private List<String> fieldNames =new ArrayList<>();
    
    public TupleType(TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("type", "expected 'type' keyword");
        typeName =ts.matchIdentifier("Expected type name");

        ts.matchStr("(", "expected '(' defining tuple data");
        for(;;) {
            fieldNames.add(ts.matchIdentifier("expected identifier for content"));
            if (ts.matchStr(")")) break;
            if (!ts.matchStr(",")) throw new Exception("Expected comma or ')'");
        }
    }


    public String getTypeName() {
        return typeName;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

}
