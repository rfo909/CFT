/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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

package rf.configtool.root.shell;

import java.io.File;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.ValueList;

public class FileSet {
    
    private HashSet<String> paths=new HashSet<String>();
    
    public static final long LS_DEFAULT_TIMEOUT_MS = 6000;
    // list files and dirs in currDir - default (no parameters) is guarded by a
    // timeout, since
    // working with remote directories, and many files, ls can take very long time.

    public static final int LS_DEFAULT_MAX_ENTRIES = 2000;


    private List<String> directoryList=new ArrayList<String>();
    private List<String> fileList=new ArrayList<String>();;
    
    private final String opName;
    private final boolean includeDirs;
    private final boolean includeFiles;
    
    private boolean isDestructiveOperation = true;
    
    private boolean argsContainGlobbing = false;
    
    private boolean isWindows() {
        return File.separatorChar=='\\';
    }

    public FileSet (String opName, boolean includeDirs, boolean includeFiles) {
        this.opName=opName;
        this.includeDirs=includeDirs;
        this.includeFiles=includeFiles;
    }
    
    /**
     * Override default value of isDestructiveOperation to false
     */
    public void setIsSafeOperation () {
        isDestructiveOperation=false;
    }
    
    public void addFilePath (String path) {
        if (!includeFiles) return;
        String key=(isWindows() ? path.toLowerCase() : path);
        if (paths.contains(key)) return; // already present
        paths.add(key);
        fileList.add(path);
    }

    public void addDirectoryPath (String path) {
        if (!includeDirs) return;
        String key=(isWindows() ? path.toLowerCase() : path);
        if (paths.contains(key)) return; // already present
        paths.add(key);
        directoryList.add(path);
    }

    
    private void addFile (ObjFile file) throws Exception {
        if (isDestructiveOperation) {
            file.validateDestructiveOperation(opName);
        }
        addFilePath( file.getFile().getCanonicalPath() );     
    }
    
    private void addDir (ObjDir dir) throws Exception {
        if (isDestructiveOperation) {
            dir.validateDestructiveOperation(opName);
        }
        addDirectoryPath ( dir.getDir().getCanonicalPath() );
    }
    
    public List<String> getFiles() {
        return fileList;
    }
    
    public List<String> getDirectories() {
        return directoryList;
    }
    
    /**
     * Default processing for ls, when no args. Contains checks on duration and number of files, to
     * avoid hanging forever. To override, supply arg, for ex '*' or some path.
     */
    public String addDirContent(String dir, ObjGlob glob, boolean enforceLimits) throws Exception {
        File f = new File(dir);

        long startTime = System.currentTimeMillis();


        DirectoryStream<Path> stream = Files.newDirectoryStream(f.toPath());

        Iterator<Path> iter = stream.iterator();
        int totalCount = 0;
        while (iter.hasNext()) {

            if (enforceLimits) {
                long duration = System.currentTimeMillis() - startTime;

                if (duration > LS_DEFAULT_TIMEOUT_MS) {
                    return "--- directory listing timed out after " + LS_DEFAULT_TIMEOUT_MS + ", use '" + opName + " .' to override";
                }
                if (totalCount >= LS_DEFAULT_MAX_ENTRIES) {
                    return "--- directory entry count > " + LS_DEFAULT_MAX_ENTRIES + ", use '" + opName + " .' to override";
                }
            }

            Path p = iter.next();

            File x = p.toFile();
            if (!glob.matches(x.getName())) continue;
            
            String path = x.getCanonicalPath();
            if (x.isFile()) {
                addFilePath(path);
            } else if (x.isDirectory()) {
                addDirectoryPath(path);
            }

            totalCount++;

        }
        return null;

    }

    public void processArg (String currentDir, Ctx ctx, Arg arg) throws Exception {
        processArg(currentDir, ctx, arg, false, false);
    }
    
    /**
     * Processing argument to shell function, doing wildcard globbing and
     * detecting absolute paths, resolving Expr if arg.isExpr()
     */
    public void processArg (String currentDir, Ctx ctx, Arg arg, boolean allowNewDir, boolean allowNewFile) throws Exception {
        if (arg.isExpr()) {
            Value v=arg.resolveExpr(ctx);
            if (v instanceof ValueString) {
                String str=((ValueString) v).getVal();
                processStringArg(currentDir, str, allowNewDir, allowNewFile);
            } else if (v instanceof ValueList) {
                List<Value> list=((ValueList) v).getVal();
                NEXT_LIST_ELEMENT: for (Value listElement:list) {
                    if (listElement instanceof ValueObj) {
                        Obj obj=((ValueObj) listElement).getVal();
                        if (obj instanceof ObjDir) {
                            addDir((ObjDir) obj);
                            continue NEXT_LIST_ELEMENT;
                        } else if (obj instanceof ObjFile) {
                            addFile((ObjFile) obj);
                            continue NEXT_LIST_ELEMENT;
                        } 
                    }
                    // can only handle files and directories
                    throw new Exception("Invalid expression list value");
                }
            } else if (v instanceof ValueObj) {
                Obj obj=((ValueObj) v).getVal();
                if (obj instanceof ObjFile) {
                    ObjFile file=((ObjFile) obj);
                    addFile(file);
                } else if (obj instanceof ObjDir) {
                    ObjDir dir=(ObjDir) obj;
                    addDir(dir);
                }
            } else {
                throw new Exception("Invalid expression value");
            }
        } else {
            processStringArg(currentDir, arg.getString(), allowNewDir, allowNewFile);
        }
    }
    
    
    private void processStringArg (String currentDir, String arg, boolean allowNewDir, boolean allowNewFile) throws Exception {

        // Pre-processing arg: the path lookup operators
        
        if (arg.startsWith("-") || arg.startsWith("+")) {
            boolean fromStart=(arg.startsWith("+"));

            String remainingArg=arg.substring(1);
            String lookupTerm;
            
            int firstSepPos=remainingArg.indexOf(File.separatorChar);
            if (firstSepPos >= 0) {
                lookupTerm=remainingArg.substring(0,firstSepPos);
                remainingArg=remainingArg.substring(firstSepPos);
            } else {
                lookupTerm=remainingArg;
                remainingArg="";
            }
            
            if (lookupTerm.length()==0) throw new Exception("Path lookup: empty lookup term: " + arg);
            
            //System.out.println("Path lookup: lookupTerm=" + lookupTerm + " remainingArg=" + remainingArg);
            
            int pathMatchPos;
            if (fromStart) {
                pathMatchPos=currentDir.indexOf(lookupTerm);
            } else {
                pathMatchPos=currentDir.lastIndexOf(lookupTerm);
            }

            if (pathMatchPos < 0) {
                throw new Exception("Path lookup: no match: " + arg);
            }
            
            int pathNextSepPos=currentDir.indexOf(File.separator,pathMatchPos);
            if (pathNextSepPos > 0) {
                // complete rewrite of arg
                arg=currentDir.substring(0,pathNextSepPos)+remainingArg;
            } else {
                // No file.separator following lookup match pos - must have matched current directory
                arg=currentDir+remainingArg;
            }
            
            //throw new Exception("Stopping");      
        }
        
        
        
        
        boolean isAbsolute;

        if (isWindows()) {
            isAbsolute=arg.startsWith("\\") || (arg.length() >= 3 && arg.charAt(1)==':' && arg.charAt(2)=='\\');
        } else {
            isAbsolute=arg.startsWith("/");
        }
        
        int lastSeparatorPos = arg.lastIndexOf(File.separatorChar);
        boolean containsSeparators = (lastSeparatorPos >= 0);
        
        int globPos = arg.lastIndexOf('*');
        boolean usesGlobbing = (globPos >= 0);
        if (usesGlobbing) argsContainGlobbing=true;
        
        if (usesGlobbing && containsSeparators && globPos < lastSeparatorPos) {
            throw new Exception("Invalid globbing pattern: " + arg);
        }
        
        if (!usesGlobbing) {
            File f;

            if (isAbsolute) {
                f=new File(arg);
            } else {
                f=new File(currentDir + File.separator + arg);
            }
            
            if (f.exists()) {
                if (f.isDirectory()) {
                    this.addDirectoryPath(f.getCanonicalPath());
                } else if (f.isFile()) {
                    this.addFilePath(f.getCanonicalPath());
                }
            } else {
                
                if (allowNewDir) {
                    this.addDirectoryPath(f.getCanonicalPath());
                }
                if (allowNewFile) {
                    this.addFilePath(f.getCanonicalPath());;
                }
                if (!allowNewDir && !allowNewFile) throw new Exception("No such file or directory: " + f.getCanonicalPath());
            }
            
            return;
        }
        
        // Uses globbing!
        
        ObjGlob glob;
        String theDir;
        
        if (!containsSeparators) {
            glob=new ObjGlob(arg);
            theDir=currentDir;
        } else {
    
            glob=new ObjGlob(arg.substring(lastSeparatorPos+1));

            if (isAbsolute) {
                theDir=arg.substring(0,lastSeparatorPos);
            } else {
                theDir=currentDir + File.separator + arg.substring(0,lastSeparatorPos);
            }
        }
        
        this.addDirContent(theDir, glob, true);
    }

    
    public boolean argsContainGlobbing() {
        return this.argsContainGlobbing;
    }
    
}
