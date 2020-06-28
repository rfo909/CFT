package rf.configtool.main;

import rf.configtool.parser.SourceLocation;

public class CodeLine {
	private SourceLocation loc;
	private String line;
	
	public boolean isWhitespace()  {
		return line.trim().length()==0;
	}
	
	public CodeLine(SourceLocation loc, String line) {
		super();
		this.loc = loc;
		this.line = line;
	}
	public SourceLocation getLoc() {
		return loc;
	}
	public String getLine() {
		return line;
	}
	


}
