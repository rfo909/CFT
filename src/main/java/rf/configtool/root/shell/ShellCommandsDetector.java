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
		"cat","edit","more",
		"touch",
			
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
			//System.out.println("Not shell command: " + line);
			return null;
		}
		
		//System.out.println("Found shell command: " + line);
		
		List<String> parts=parseLineParts();
		
		// show parts
		for (String s:parts) {
			System.out.println("[" + s + "]");
		}
		
		String name=parts.get(0);
		
		
		if (name.equals("ls") || name.equals("lsd") || name.equals("lsf")) {
			return new ShellLs(parts);
		}
		if (name.equals("cd")) {
			return new ShellCd(parts);
		}
		if (name.equals("cat") || name.equals("edit") || name.equals("more")) {
			return new ShellCatEditMore(parts);
		}
		if (name.equals("touch")) {
			return new ShellTouch(parts);
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
