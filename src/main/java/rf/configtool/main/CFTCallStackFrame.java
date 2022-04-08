package rf.configtool.main;

import rf.configtool.lexer.SourceLocation;

public class CFTCallStackFrame {
	
	private String str;
	
	public CFTCallStackFrame (String location) {
		this(location,null);
	}
	
	public CFTCallStackFrame (String location, String description) {
		this.str=location + (description != null ? " " + description : "");
	}
	
	public CFTCallStackFrame (SourceLocation location, String description) {
		this(location.toString(), description);
	}
	
	public String toString() {
		return str;
	}
	

}
