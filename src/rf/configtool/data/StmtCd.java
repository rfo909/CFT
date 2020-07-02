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
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class StmtCd extends Stmt {

    private Expr dirExpr;
    private List<String> pathElements;
    
    public StmtCd (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("cd","expected 'cd'");
        if (ts.matchStr("(")) {
            dirExpr=new Expr(ts);
            ts.matchStr(")", "expected ')' closing cd(...)");
            return;
        }
        pathElements=new ArrayList<String>();
        while (!ts.atEOF()) {
            Token t=ts.matchAnyToken("internal error");
            pathElements.add(t.getStr());
        }
    }

    public void execute (Ctx ctx) throws Exception {
        if (dirExpr != null) {
            Value v=dirExpr.resolve(ctx);
            if (!(v instanceof ValueObj)) throw new Exception("Expected ObjDir");
            Obj obj=((ValueObj) v).getVal();
            if (!(obj instanceof ObjDir)) throw new Exception("Expected ObjDir");
            ObjDir dir=(ObjDir) obj;
            if (!dir.dirExists()) {
                throw new Exception("No such directory");
            }
            ctx.getObjGlobal().setCurrDir(dir.getName());
        } else if (pathElements.size()==0) {
            ctx.getObjGlobal().setCurrDir(null);
        } else if (pathElements.size()==1) {
            String path=pathElements.get(0);
            
            File f;
            if (path.startsWith(File.separator) || (path.length()>1 && path.charAt(1)==':') ) {
                // absolute path
                f=new File(path);
            } else {
                f=locateDir(ctx, pathElements);
            }
            if (f==null || !f.isDirectory() || !f.exists()) {
                throw new Exception("No such directory");
            }
            String currDir=f.getCanonicalPath();
            
            ctx.getObjGlobal().setCurrDir(currDir);
        } else {
                File f=locateDir(ctx,pathElements);

                if (f==null || !f.isDirectory() || !f.exists()) {
                    throw new Exception("No such directory");
                }
                String currDir=f.getCanonicalPath();
                
                ctx.getObjGlobal().setCurrDir(currDir);
        }
        String currDir=ctx.getObjGlobal().getCurrDir();
        ctx.getOutText().addPlainText(currDir);
        ctx.push(new ValueObj(new ObjDir(currDir)));

    }
    
    private File locateDir (Ctx ctx, List<String> pathElements) throws Exception {
        if (pathElements.size()==1) {
            File f=new File(ctx.getObjGlobal().getCurrDir() + File.separator + pathElements.get(0));
            if (f.exists()) return f;
        }
        
        File currDir=new File(ctx.getObjGlobal().getCurrDir());
        
        File f=null;
        
        for (File content:currDir.listFiles()) {
            if (!content.isDirectory()) continue;
            boolean match=true;
            for (String path:pathElements) {
                if (!content.getName().contains(path)) match=false;
            }
            if (match) {
                if (f != null) throw new Exception("Not unique match");
                f=content;
            }
        }
        return f;
    }
    
    

}
