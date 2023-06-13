package rf.configtool.lexer;

/** 
 * Adding a StopRule.STOP CharTable is a way of creating exceptions to the defaultMapping
 * inside CharTable, signaling invalid characters.
 *
 */
public class StopRule {

	public static final CharTable STOP = new CharTable();	
}
