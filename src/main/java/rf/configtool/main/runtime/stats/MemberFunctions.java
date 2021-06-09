package rf.configtool.main.runtime.stats;

import java.util.*;
import java.io.*;

public class MemberFunctions {

	public static String FILE = "Stats.txt";
	private static List<String> buffer=new ArrayList<String>();
	
	public static void call (String className, String funcName) {
		buffer.add(className + "|"+funcName);
		if (buffer.size() >= 100) sync();
	}
	
	private static void sync() {
		PrintStream ps=null;
		try {
			File f=new File(FILE);
			ps=new PrintStream(new FileOutputStream(f,true));
			for (String line:buffer) {
				ps.println(line);
			}
			buffer.clear();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ps != null) try {ps.close();} catch (Exception e) {}
		}
	}
	
}
