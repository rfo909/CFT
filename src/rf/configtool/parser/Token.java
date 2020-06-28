package rf.configtool.parser;

public class Token {
    
    public static final int TOK_IDENTIFIER = 1;
    public static final int TOK_INT = 2;
    public static final int TOK_FLOAT = 3;
    public static final int TOK_STRING = 4;
    public static final int TOK_SPECIAL = 5;
    public static final int TOK_EOF = 99;
    
    private int type; 
    private String str;
    private SourceLocation loc;
    
    
    public Token (SourceLocation loc, int type, String str) {
        this.loc=loc;
        this.type=type;
        if (type==TOK_STRING) {
            str=str.substring(1,str.length()-1);  // strip quotes
        }
        this.str=str;
    }
    
    public boolean matchStr (String s) {
        if (type==TOK_STRING) return false;
        return str.equals(s);
    }
    
    public boolean matchType (int type) {
        return this.type==type;
    }

    public String getStr() {
        return str;
    }
    
    public int getType() {
        return type;
    }
    
    public SourceLocation getSourceLocation() {
        return loc;
    }
}
