/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import rf.configtool.lexer.SourceLocation;

public class PropsFile {
    
    public static final String DEFAULT_SHORTCUT = "'Undefined shortcut'";

    public static final String PROPS_FILE = "CFT.props";
    
    private String customScriptDir; // -d xxx on command line, see Main
    
    private String codeDirs;
    private String prompt;   // line of code
    private String bangCommand; // line of code
    
    private String shell;
    private String winShell;
    
    private String mCat;
    private String mEdit;
    private String mMore;
    private String mTail;
    private String mRm;
    private String mDiff;
    private String mShowtree;
    private String mHash;
    
    private String mSymGet;
    private String mSymSet;
    
    private String shortcutPrefix;
    private Map<String,String> shortcuts;
    
    private String db2Dir;
    private String globalOnLoad;
    

    // detect changes 
    private String fileInfo="";
    
    public PropsFile () throws Exception {
        refreshFromFile();
    }

    public PropsFile (String customScriptDir) throws Exception {
        this.customScriptDir=customScriptDir;
        refreshFromFile();
    }
    
    
    private String fixDir (String s) {
        return s.replace("/", File.separator);
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
        prompt="'$ '";  // code
        bangCommand="println('bangCommand not defined')"; // code
        
        shell = "/usr/bin/bash";
        winShell = "powershell";
        
        // code
        mCat  = "Lambda {error('mCat lambda undefined in " + PROPS_FILE + "') }";
        mEdit = "Lambda {error('mEdit lambda undefined in " + PROPS_FILE + "') }";
        mMore = "Lambda{error('mMore lambda undefined in " + PROPS_FILE + "') }";
        mTail = "Lambda{error('mTail lambda undefined in " + PROPS_FILE + "') }";
        mRm = "Lambda{error('mRm lambda undefined in " + PROPS_FILE + "') }";
        mDiff = "Lambda{error('mDiff lambda undefined in " + PROPS_FILE + "') }";
        mShowtree = "Lambda{error('mShowtree lambda undefined in " + PROPS_FILE + "') }";
        mHash = "Lambda{error('mHash lambda undefined in " + PROPS_FILE + "') }";
        
        mSymGet = "Lambda{error('mSymGet lambda undefined in " + PROPS_FILE + "') }";
        mSymSet = "Lambda{error('mSymSet lambda undefined in " + PROPS_FILE + "') }";
        
        
        shortcutPrefix = "@";
        shortcuts=new HashMap<String,String>();

        db2Dir = "Db";
        
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
                    
                    if (name.equals("codeDirs")) {
                        codeDirs=fixDir(value);
                    } else 
                    if (name.equals("prompt")) {
                        prompt=value;
                    } else
                    if (name.equals("bangCommand")) {
                        bangCommand=value;
                    } else 
                    if (name.equals("shell")) {
                        shell=value;
                    } else
                    if (name.equals("winShell")) {
                        winShell=value;
                    } else
                    
                    if (name.equals("mCat")) {
                        mCat=value;
                    } else
                    if (name.equals("mEdit")) {
                        mEdit=value;
                    } else 
                    if (name.equals("mMore")) {
                        mMore=value;
                    } else
                    if (name.equals("mTail")) {
                    	mTail=value;
                    } else
                    if (name.equals("mRm")) {
                    	mRm=value;
                    } else
                    if (name.equals("mDiff")) {
                    	mDiff=value;
                    } else
                    if (name.equals("mHash")) {
                    	mHash=value;
                    } else
                    if (name.equals("mShowtree")) {
                    	mShowtree=value;
                    } else
                    if (name.equals("mSymGet")) {
                        mSymGet=value;
                    } else
                    if (name.equals("mSymSet")) {
                        mSymSet=value;
                    } else

                    if (name.equals("shortcutPrefix")) {
                        shortcutPrefix=value;
                    } else
                    if (name.startsWith("shortcut:")) {
                        int colonPos=name.indexOf(':');
                        String shortcutName=name.substring(colonPos+1);
                        shortcuts.put(shortcutName, value);  // macro
                    } else
                    if (name.equals("db2Dir")) {
                        db2Dir=fixDir(value);
                    } else if (name.equals("globalOnLoad")) {
                        globalOnLoad=value;
                    } else {
                        System.out.println("Invalid configuration field: " + name);
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
    
    public String getBangCommand() {
        return bangCommand;
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
    
    public String getMTail() {
    	return mTail;
    }
    
    public String getMRm() {
    	return mRm;
    }
    
    public String getMDiff() {
    	return mDiff;
    }
    
    public String getMShowtree () {
    	return mShowtree;
    }
    
    public String getMHash () {
    	return mHash;
    }
    
    public String getMSymGet () {
    	return mSymGet;
    }
    
    public String getMSymSet () {
    	return mSymSet;
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
    
    public File getScriptSavefile (String name, File currDir) throws Exception {
        {
            File f=new File(currDir.getCanonicalPath() + File.separator + name);
            if (f.exists()) return f;
        }
        
        for (String s:getCodeDirs()) {
            createDir(s);
            File f = new File(s + File.separator + name);
            if (f.exists()) return f;
        }
        
        throw new Exception("No such file: " + name);
    }
    
    public String getDb2Dir() {
        return db2Dir;
    }
    
    public String getGlobalOnLoad() {
        return globalOnLoad;
    }

}
