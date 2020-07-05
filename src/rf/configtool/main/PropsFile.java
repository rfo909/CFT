package rf.configtool.main;

import java.io.*;
import java.util.*;

public class PropsFile {

	public static final String PROPS_FILE = "CFT.props";
	
	private String codeDirs;
	
	public PropsFile () throws Exception {
		
		File f=new File(PROPS_FILE);
		
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(f));
			for (;;) {
				String line=br.readLine();
				if (line==null) break;
				
				if (line.startsWith("#")) continue;
			
				int pos=line.indexOf('=');
				if (pos >= 0) {
					String name=line.substring(0,pos).trim();
					String value=line.substring(pos+1).trim();
					
					if (name.equals("codeDirs")) codeDirs=value;
				}
			}
		} finally {
			if (br != null) try {br.close();} catch (Exception ex) {};
		}
		
	}
	
	/**
	 * Return parsed list of code dirs 
	 */
	public List<String> getCodeDirs() {
		List<String> list=new ArrayList<String>();
		StringTokenizer st=new StringTokenizer(codeDirs,";",false);
		while (st.hasMoreElements()) {
			list.add(st.nextToken().trim());
		}
		return list;
	}
	
	/**
	 * Current working directory is first directory in codeDirs
	 */
	private String getSaveDir() {
		List<String> list=getCodeDirs();
		return list.get(0); 
	}
	
	public void report (Stdio stdio) {
		stdio.println("[PropsFile] codeDirs: " + codeDirs);
	}
	
	private void createDir (String path) throws Exception {
		File f=new File(path);
		if (!f.exists()) {
			boolean ok = f.mkdir();
			if (!ok) throw new Exception("Could not create directory " + f.getCanonicalPath());
		}
		if (!f.isDirectory()) {
			throw new Exception("Invalid directory: " + f.getCanonicalPath());
		}
	}
	
	public File getCodeSaveFile (String name) throws Exception {
		createDir(getSaveDir());
		return new File(getSaveDir() + File.separator + name);
	}

	public File getCodeLoadFile (String name) throws Exception {
		for (String s:getCodeDirs()) {
			createDir(s);
			File f = new File(s + File.separator + name);
			if (f.exists()) return f;
		}
		throw new Exception("No such file: " + name);
	}

}
