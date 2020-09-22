package rf.configtool.main.runtime.lib.text;

import rf.configtool.main.runtime.Value;

/**
 * The Parser front ends handle all that has to do with matching tokens. 
 *
 * Tokens in Parser productions are called token selection strings. They
 * are prefixed by '%' and have three parts, all optional. 
 * 
 * %[tokenTypeName][=x][:'...']
 * 
 * Token type names are defined differently for the two front ends. For the regular front end, token type names
 * are identified via the integer tokenType, while for ad-hoc front end, via direct pointers to root Node for that
 * single token type.
 * 
 * The second is an optional name to store the match in the resulting data structure.
 * 
 * The last part is a lexical match, and is really  <colon><endChar>...<endChar>.
 * 
 * ---
 * The idea is that the Parser processes grammar only, and does not know how to match as well as backtrack
 * token matches. 
 */
public interface FrontEnd {
	
	public FrontEndState getInputPos();
	public void setInputPos(FrontEndState mark);
	
	/**
	 * Match token and return Value, or Java null if failing
	 */
	public Value matchToken (String tokenSelectionString) throws Exception;
	
	
	public String getSourceLocation();
	

}
