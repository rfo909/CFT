package rf.configtool.root.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import rf.configtool.lexer.*;
import rf.configtool.main.ScriptSourceLine;

/**
 * Detecting and executing the "shell" commands: ls, cd, pwd ...
 *
 */
public class ShellCommandsDetector {
	
	private final String line;
	
	public static final String[] OPS = {
		"ls","lsd","lsf",
		"cd",
		"cat","edit","more","tail",
		"touch",
		"mv",
		"cp",
		"rm",
		"diff",
		"mkdir",
			
	};

	public ShellCommandsDetector (String line) throws Exception {
		this.line=line.trim();
	}
	
	public ShellCommand identifyShellCommand () throws Exception {
		boolean found=false;
		
		// do simple string comparison to avoid running full parse for
		// all interactive commands
		for (String op:OPS) {
			if (line.equals(op) || line.startsWith(op+" ") || line.startsWith(op+"("))  found=true;
		}
		
		if (!found) {
			return null;
		}
		
		List<String> parts=parseLineParts();
		
		String name=parts.get(0);
		
		
		if (name.equals("ls") || name.equals("lsd") || name.equals("lsf")) {
			return new ShellLs(parts);
		}
		if (name.equals("cd")) {
			return new ShellCd(parts);
		}
		if (name.equals("cat") || name.equals("edit") || name.equals("more") || name.equals("tail")) {
			return new ShellCatEditMoreTail(parts);
		}
		if (name.equals("touch")) {
			return new ShellTouch(parts);
		}
		if (name.equals("mv")) {
			return new ShellMv(parts);
		}
		if (name.equals("cp")) {
			return new ShellCp(parts);
		}
		if (name.equals("rm")) {
			return new ShellRm(parts);
		}
		if (name.equals("diff")) {
			return new ShellDiff(parts);
		}
		if (name.equals("mkdir")) {
			return new ShellMkdir(parts);
		}
		// else ...
		
		throw new Exception("Internal error: invalid ShellParser operation name=" + name);
		

		
	}
	

	/**
	 * Group line into parts, and unwrap strings outside ()'s but not inside.
	 * Separate by space (outside strings and ()'s)
	 */
	private List<String> parseLineParts () throws Exception {
		List<String> parts=new ArrayList<String>();
		
		int parCount=0;
		boolean inString=false;
		char strQuote=' ';
		
		StringBuffer sb=new StringBuffer();
		
		// Note: there is a weakness in the parser, as it does not recognize symbol lookups
		// particularly, which means that for example:
		// 
		// cd %someDir.sub("data")
		//
		// will not work, as this is split into the following parts: [cd] [%somedir.sub] [("data")]
		//
		// It is not an important issue, so not complicating matters over this now (v3.5.5)
		
		CHARS: for (int pos=0; pos<line.length(); pos++) {
			final char c=line.charAt(pos);
			
			if (inString) {
				if (c==strQuote) {
					inString=false;
				} else {
					sb.append(c);
				}
				continue CHARS;
			}
			
			// !inString
				
			if (parCount==0 && (c=='\'' || c=='"')) {
				inString=true;
				strQuote=c;
				continue CHARS;
			}
			
			if (parCount > 0 || c=='(') {
				if (parCount==0 && sb.length() > 0) {
					parts.add(sb.toString());
					sb=new StringBuffer();
				}
				if (c=='(') { 
					sb.append(c); 
					parCount++; 
				} else if (c==')') {
					sb.append(c); 
					parCount--; 
					if (parCount == 0) {
						parts.add(sb.toString());
						sb=new StringBuffer();
					}
				} else {
					sb.append(c);
				}
				continue CHARS;
			} 

			// outside ()'s parts are separated by space
			if (c==' ') {
				if (sb.length()>0) {
					parts.add(sb.toString());
					sb=new StringBuffer();
					continue CHARS;
				}
			} else {
				sb.append(c);
			}
		}
		if (parCount > 0) {
			throw new Exception("Unbalanced ()'s");
		}
		if (inString) {
			throw new Exception("Unterminated string");
		}
		if (sb.length()>0) {
			parts.add(sb.toString());
		}
		
		return parts;
	}
	
	

	
	
	

}
