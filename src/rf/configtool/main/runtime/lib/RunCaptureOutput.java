package rf.configtool.main.runtime.lib;

import java.util.*;

import rf.configtool.main.runtime.*;

/**
 * Used by Dir.runCapture() function
 */
public class RunCaptureOutput {
	private List<String> lines=new ArrayList<String>();
	
	public void addLine (String line) {
		lines.add(line);
	}
	
	public Value getCapturedLines() {
		List<Value> stdoutLines=new ArrayList<Value>();
		for (String s:lines) stdoutLines.add(new ValueString(s));
		
		return new ValueList(stdoutLines);
	}
}

