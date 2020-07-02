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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.parser.TokenStream;

/**
 * Create directory listing for current directory (copy of ObjDir.dir())
 */
public class ExprLs extends LexicalElement {

    private Expr exprPos;
    private boolean showFiles;
    private boolean showDirs;
    
    
    public ExprLs (TokenStream ts) throws Exception {
        super(ts);
        if (ts.matchStr("ls")) {
            showFiles=showDirs=true;
        } else if (ts.matchStr("lsf")) {
            showFiles=true;
        } else {
            ts.matchStr("lsd", "expected ls, lsf or lsd");
            showDirs=true;
        }
        
        if (ts.matchStr("(")) {
            exprPos=new Expr(ts);
            ts.matchStr(")", "expected ')' following exprPos");
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        ObjGlob glob=null;
        
        Comparator<String> c=new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        };
        

        String currDir=ctx.getObjGlobal().getCurrDir();

        File f=new File(currDir);
        List<String> directories=new ArrayList<String>();
        List<String> files=new ArrayList<String>();

        for (String s:f.list()) {
            File x=new File(currDir + File.separator + s);
            if (x.isFile()) {
                if (glob != null && !glob.matches(s)) continue;
                files.add(s);
            } else if (x.isDirectory()) {
                directories.add(s); // no globbing 
            }
            
        }
        if (showDirs) {
            directories.sort(c);
        } else {
            directories.clear();
        }
        if (showFiles) {
            files.sort(c);
        } else {
            files.clear();
        }
        
        
        if (exprPos != null) {
            // return element at given pos in result list 
            Value v=exprPos.resolve(ctx);
            if (v == null || !(v instanceof ValueInt)) {
                throw new Exception("Invalid parameter value, expected int");
            }
            long pos=((ValueInt) v).getVal();
            // data are presented directories first, then files
            if (pos<directories.size()) {
                return new ValueObj(new ObjDir(currDir + File.separator + directories.get((int) pos)));
            } 
            pos -= directories.size();
            return new ValueObj(new ObjFile(currDir + File.separator + files.get((int) pos)));

        }
        
        List<Value> result=new ArrayList<Value>();
        for (String x:directories) result.add(new ValueObj(new ObjDir(currDir + File.separator + x)));
        for (String x:files) result.add(new ValueObj(new ObjFile(currDir + File.separator + x)));
                
        return new ValueList(result);
    }
}
