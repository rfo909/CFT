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

package rf.configtool.parsetree;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;

public class StmtTouch extends StmtShellInteractive {

    public StmtTouch (TokenStream ts) throws Exception {
        super(ts);
    }
    

    @Override
    protected void processDefault(Ctx ctx) throws Exception {
    	throw new Exception("Expected file to create");
    }
    
    @Override
    protected void processOne (Ctx ctx, File file) throws Exception {
    	if (!file.exists()) {
            file.createNewFile();
    	} else {
			Path path=file.toPath();
			FileTime ft = FileTime.fromMillis(System.currentTimeMillis());
			Files.setLastModifiedTime(path, ft);
    	}
    	Value result=new ValueObj(new ObjFile(file.getCanonicalPath(), Protection.NoProtection));
    	ctx.push(result);
    }
    
    
    @Override
    protected void processSet (Ctx ctx, List<File> elements) throws Exception {
    	for (File file:elements) processOne(ctx,file);
    }
    
    @Override
    protected boolean processUnknown (Ctx ctx, File file) throws Exception {
        file.createNewFile();
        Value result=new ValueObj(new ObjFile(file.getCanonicalPath(), Protection.NoProtection));
        ctx.push(result);;
        return true;
    }
    
   

}
