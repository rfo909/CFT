package rf.configtool.main;

import java.util.*;

import rf.configtool.main.runtime.ValueString;
import rf.configtool.parser.SourceLocation;

public class CodeInlineDocument {
	private String eofMark;
	private List<String> lines;
	private SourceLocation loc;
	
	public CodeInlineDocument (String eofMark, SourceLocation loc) {
		this.lines=new ArrayList<String>();
		this.eofMark=eofMark;
		this.loc=loc;
	}

	public void addLine (String line) {
		lines.add(line);
	}
	
	public boolean matchesEofMark (String eof) {
		return eof.equals(eofMark);
	}
	
    public String createCodeLine () {
        StringBuffer sb=new StringBuffer();
        sb.append("List(");
        boolean comma=false;
        for (String s:lines) {
            if (comma) sb.append(",");
            try {
                sb.append((new ValueString(s)).synthesize());
            } catch (Exception ex) {
                throw new RuntimeException("Should never happen");
            }
            comma=true;
        }
        sb.append(")");
        return sb.toString();
    }


}
