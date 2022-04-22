package rf.configtool.main;

import rf.configtool.lexer.TokenStream;

public class ClassDetails {
	
	private String name;
	private String type;
	
	/**
	 * Parse /class Ident ... string
	 */
	public ClassDetails (TokenStream ts) throws Exception {
		final String msg="expected '/class Name [as Type]'";
		ts.matchStr("/", msg + " - expected '/'");
		ts.matchStr("class", msg + " - expected keyword 'class'");
		name=ts.matchIdentifier(msg + " - expected Name");
		if (ts.matchStr("as")) {
			type=ts.matchIdentifier(msg + " - expected Type");
		}
	}
	
	
	/**
	 * Generate /class Ident ... string
	 */
	public String createClassDefString () {
		return "/class " + name + (type != null ? " as " + type : "");
	}
	
	
	public ClassDetails (String name, String type) {
		this.name=name;
		this.type=type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		if (type==null) return name;
		return type;
	}

}
