package rf.configtool.main.runtime.lib.text;

public class ObjLexerNodeIdentifier extends ObjLexerNode {
	
	public static final String firstChars="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
	public static final String innerChars=firstChars+"0123456789";
	
    public ObjLexerNodeIdentifier() {
    	super(firstChars);
    	setMapping(innerChars, this.getCharTable());
    }

}
