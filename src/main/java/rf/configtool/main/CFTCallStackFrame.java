package rf.configtool.main;

import java.util.*;
import rf.configtool.lexer.SourceLocation;

public class CFTCallStackFrame {
	
	private String str;
	private List<String> debugLines=new ArrayList<String>();
	
	public CFTCallStackFrame (String location) {
		this(location,null);
	}
	
	public CFTCallStackFrame (String location, String description) {
		this.str=location + (description != null ? " " + description : "");
	}
	
	public CFTCallStackFrame (SourceLocation location, String description) {
		this(location.toString(), description);
	}
	
	public void addDebugLine (String debugLine) {
		if (debugLines.size()==0) {
			debugLines.add(debugLine);
		} else {
			debugLines.add(0, debugLine); // newest first
		}
	}
	
	public List<String> getDebugLines() {
		return debugLines;
	}
	
	public String toString() {
		return str;
	}
	

}
