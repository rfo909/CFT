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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import rf.configtool.util.TabUtil;

import java.util.*;

public class StmtCd extends StmtShellInteractive {

    public StmtCd (TokenStream ts) throws Exception {
        super(ts);
        if (!getName().equals("cd")) throw new Exception("Expected cd");
    }

    @Override
    protected void processDefault(Ctx ctx) throws Exception {
        ctx.getObjGlobal().setCurrDir(null);
        ctx.getObjGlobal().addSystemMessage(ctx.getObjGlobal().getCurrDir());
        ctx.push(new ValueObj(new ObjDir(ctx.getObjGlobal().getCurrDir(), Protection.NoProtection)));
    }
    
    
    @Override
    protected void processOne (Ctx ctx, File file) throws Exception {
        if (file.exists() && file.isDirectory()) {
            ctx.getObjGlobal().setCurrDir(file.getCanonicalPath());
            ctx.getObjGlobal().addSystemMessage(ctx.getObjGlobal().getCurrDir());
            ctx.push(new ValueObj(new ObjDir(ctx.getObjGlobal().getCurrDir(), Protection.NoProtection)));
        } else {
            throw new Exception("cd: Invalid directory");
        }
    
    }
    
    
    
    @Override
    protected void processSet (Ctx ctx, List<File> elements) throws Exception {
        if (elements.size() == 0) throw new Exception("cd: expected one directory");
        if (elements.size() != 1) throw new Exception("cd: can only process one directory");
        processOne(ctx, elements.get(0));
   }
    
   
}
