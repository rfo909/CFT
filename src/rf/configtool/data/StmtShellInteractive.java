/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

package rf.configtool.data;

import java.io.File;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

/**
 * Superclass of all interactive file and directory statements
 * 
 * StmtLs tested ok
 * StmtCat tested ok
 * StmtCd tested ok
 * 
 * edit, touch, rm
 * mkdir, rmdir
 */
public abstract class StmtShellInteractive extends Stmt {

	private final String name;
	
    private Expr expr;
    private List<String> elements;

    public StmtShellInteractive (TokenStream ts) throws Exception {
        super(ts);
        name=ts.matchIdentifier();
        
        if (ts.matchStr("(")) {
            expr=new Expr(ts);
            ts.matchStr(")", "expected ')' closing " + name + "(...)");
            return;
        }
        elements=new ArrayList<String>();
        while (!ts.atEOF()) {
            Token t=ts.matchAnyToken("internal error");
            elements.add(t.getStr());
        }
    }
    
    public String getName() {
    	return name;
    }
    
    private String getElementsString() {
    	StringBuffer sb=new StringBuffer();
    	for (String s:elements) {
    		sb.append(s);
    	}
    	return sb.toString();
    }
    
    private boolean isWindows() {
    	return File.separator.equals("\\");
    }
    
    private boolean isDriveLetter (Character c) {
    	return "abcdefghijklmnopqrstuvwxyz".indexOf(Character.toLowerCase(c)) >= 0;
    }
    
    private boolean isAbsolutePath (String s) {
    	if (isWindows()) {
    		if (s.startsWith("\\")) return true;
    		if (s.length() >= 3 && isDriveLetter(s.charAt(0)) && s.charAt(1)==':' && s.charAt(2)=='\\') return true;
    	} else {
    		if (s.startsWith("/")) return true;
    	}
    	return false;
    }
 
    /**
     * No args
     */
    protected abstract void processDefault(Ctx ctx) throws Exception;

    /**
     * The ( expr ) variant - File may or may not exist
     */
    protected abstract void processOne (Ctx ctx, File file) throws Exception;

    /**
     * Globbing or substrings - all files exist (dirs or files)
     */
    protected abstract void processSet (Ctx ctx, List<File> files) throws Exception;
    
    public void execute (Ctx ctx) throws Exception {
    	
        if (expr != null) {
            Value v=expr.resolve(ctx);
            if (!(v instanceof ValueObj)) throw new Exception("Expected File or Dir expression");
            Obj obj=((ValueObj) v).getVal();
            if (obj instanceof ObjFile) {
            	processOne( ctx, ((ObjFile) obj).getFile() );
            	return;
            }
            if (obj instanceof ObjDir) {
            	processOne( ctx, ((ObjDir) obj).getDir() );
            	return;
            }
            throw new Exception("Expected File or Dir expression");
        }
        
        if (elements.size()==0) {
        	processDefault(ctx);
        	return;
        }
        
    	String str=getElementsString();
    	
    	if (isAbsolutePath(str)) {
    		processPathExpression(ctx, str);
    		return;
    	}
    	
    	// relative path
		processPathExpression(ctx, ctx.getObjGlobal().getCurrDir() + File.separator + str);

    	
    }
    
    
    protected void sort (List<String> data) {
        Comparator<String> c=new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        };
        data.sort(c);

    }
    
    

    private void processPathExpression (Ctx ctx, String expr) throws Exception {
    	// single file or directory - as-is 
		File f=new File(expr);
		if (f.exists()) {
			processOne(ctx, f);
			return;
		}

		int pos=expr.lastIndexOf(File.separator);
		String path = expr.substring(0,pos);
		String name = expr.substring(pos+1);
		
		if (!path.contains(File.separator)) path += File.separator; 
			// as the split above leaves path for root on windows becomes "c:" or "", and for linux ""
		
		
		if (path.contains("*")) {
			throw new Exception("Invalid path: " + path);
		}
		
		File dir=new File(path);
		if (!dir.isDirectory()) {
			throw new Exception("No such directory: " + path);
		}
		
		if (name.contains("*")) { // globbing
			processSet(ctx, getGlobbedElementList(path, name));
			return;
		}
		
		// attempts substring(s)
		processSet(ctx, getElementListBySubstrings(path, elements));
	}

  
    private List<File> getGlobbedElementList (String dir, String globExpr) throws Exception {
    	File f=new File(dir);
    	ObjGlob glob=new ObjGlob(globExpr);
    	
    	if (!f.isDirectory()) throw new Exception("Not a directory: " + dir);
    	List<File> result=new ArrayList<File>();
    	for (File content: f.listFiles()) {
    		boolean ok=glob.matches(content.getName());
    		if (ok) result.add(content);
    	}
    	return result;
    }
    
    private List<File> getElementListBySubstrings(String dir, List<String> parts) throws Exception {
    	File f=new File(dir);
    	if (!f.isDirectory()) throw new Exception("Not a directory: " + dir);
    	List<File> result=new ArrayList<File>();
    	for (File content: f.listFiles()) {
    		boolean ok=true;
    		for (String p:parts) {
    			if (!content.getName().contains(p)) ok=false;
    		}
    		if (ok) result.add(content);
    	}
    	return result;
    }

}
