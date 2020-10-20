package rf.configtool.main;

import java.io.*;
import java.util.*;

import rf.configtool.parser.SourceLocation;

public class PropsFile {
	
	public static final String DEFAULT_SHORTCUT = "'Undefined shortcut'";

	public static final String PROPS_FILE = "CFT.props";
	
	private String customScriptDir; // -d xxx on command line, see Main
	
	private String codeDirs;
	private String prompt;   // line of code
	
	private String shell;
	private String winShell;
	
	private String mCat;
	private String mEdit;
	private String mMore;
	
	private String shortcutPrefix;
	private Map<String,String> shortcuts;
	

	// detect changes 
	private String fileInfo="";;

	public PropsFile (String customScriptDir) throws Exception {
		this.customScriptDir=customScriptDir;
		refreshFromFile();
	}
	
	
	public void refreshFromFile() throws Exception {
		File f=new File(PROPS_FILE);

		// Decide if file has changed
		String newFileInfo=f.length() + "x" + f.lastModified();
		if (newFileInfo.equals(fileInfo)) return;

		// going ahead with reading file
		fileInfo=newFileInfo;
		
		// Set defaults
		codeDirs=".";
		prompt="$";
		shell = "/usr/bin/bash";
		winShell = "powershell";
		
		mCat  = "{* error('mCat macro undefined in " + PROPS_FILE + "') }";
		mEdit = "{* error('mEdit macro undefined in " + PROPS_FILE + "') }";
		mMore = "{* error('mMore macro undefined in " + PROPS_FILE + "') }";
		
		shortcutPrefix = "@";
		shortcuts=new HashMap<String,String>();

		// process file
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
					if (name.equals("prompt")) prompt=value;
					
					if (name.equals("shell")) shell=value;
					if (name.equals("winShell")) winShell=value;
					
					if (name.equals("mCat")) mCat=value;
					if (name.equals("mEdit")) mEdit=value;
					if (name.equals("mMore")) mMore=value;
					
					if (name.equals("shortcutPrefix")) shortcutPrefix=value;
					if (name.startsWith("shortcut:")) {
						int colonPos=name.indexOf(':');
						String shortcutName=name.substring(colonPos+1);
						shortcuts.put(shortcutName, value);  // macro
					}
				}
			}
		} finally {
			if (br != null) try {br.close();} catch (Exception ex) {};
		}
		
	}
	
	public SourceLocation getSourceLocation (String value) throws Exception {
		return new SourceLocation(PROPS_FILE, 0, 0);
		
	}
	
	/**
	 * Return parsed list of code dirs 
	 */
	public List<String> getCodeDirs() {
		List<String> list=new ArrayList<String>();
		if (customScriptDir != null) list.add(customScriptDir);
		StringTokenizer st=new StringTokenizer(codeDirs,";",false);
		while (st.hasMoreElements()) {
			list.add(st.nextToken().trim());
		}
		return list;
	}
	
	public String getPromptCode() {
		return prompt;
	}
	
	public String getShell() {
		return shell;
	}
	
	public String getWinShell() {
		return winShell;
	}
	
	public String getMCat() {
		return mCat;
	}
	
	public String getMEdit() {
		return mEdit;
	}
	
	public String getMMore() {
		return mMore;
	}
	
	
	public String getShortcutPrefix() {
		return shortcutPrefix;
	}
	
	public String getShortcutCode (String shortcutName) {
		String s=shortcuts.get(shortcutName);
		if (s==null) s=DEFAULT_SHORTCUT;
		return s;
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
