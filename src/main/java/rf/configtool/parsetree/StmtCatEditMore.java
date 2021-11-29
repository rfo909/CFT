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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CodeLine;
import rf.configtool.main.CodeLines;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.OutText;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.util.TabUtil;

import java.util.*;

/**
 * Configured via CFT.props mCat, mEdit and mMore fields, are all macros. 
 * The macros take a file as parameter, which may be null. 
 */
public class StmtCatEditMore extends StmtShellInteractive {

    private String name;
    
    public StmtCatEditMore (TokenStream ts) throws Exception {
        super(ts);
        this.name = getName();
        if (!name.equals("cat") && !name.equals("edit") && !name.equals("more")) throw new Exception("Expected cat, edit or more");
    }

    @Override
    protected void processDefault(Ctx ctx) throws Exception {
        // Run edit macro without args
        callMacro(ctx, null);
    }
    
    
    @Override
    protected void processOne (Ctx ctx, File file) throws Exception {
        if (file.exists() && file.isFile()) {
            ObjFile objFile = new ObjFile(file.getCanonicalPath(), Protection.NoProtection);
            // Call the macro corresponding to the name 
            callMacro(ctx,objFile);
      } else {
            throw new Exception("Invalid file");
        }
    
    }
    
    
    
    @Override
    protected void processSet (Ctx ctx, List<File> elements) throws Exception {
        if (elements.size() == 0) throw new Exception("Expected one file");
        if (elements.size() != 1) throw new Exception("Can only process one file");
        processOne(ctx, elements.get(0));
   }
    
 
    
    private void callMacro (Ctx ctx, ObjFile file) throws Exception {
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        SourceLocation loc=propsFile.getSourceLocation(name);
        
        String macro;
        String fieldName;

        if (name.equals("cat")) {
            macro=propsFile.getMCat();
            fieldName="mCat";
        } else if (name.equals("edit")) {
            macro=propsFile.getMEdit(); 
            fieldName="mEdit";
        } else if (name.equals("more")) {
            macro=propsFile.getMMore();
            fieldName="mMore";
        } else {
            throw new Exception("Invalid statement name, expected edit or more: " + name);
        }

        //OutText out = ctx.getOutText();
        //out.addSystemMessage("Running " + PropsFile.PROPS_FILE + "." + fieldName + " macro: " + macro);
        
        CodeLines codeLines=new CodeLines(macro, loc);
        
        Value ret = ctx.getObjGlobal().getRuntime().processCodeLines(ctx.getStdio(), codeLines, new FunctionState());
        if (!(ret instanceof ValueBlock)) throw new Exception("Not a macro: " + macro + " ---> " + ret.synthesize());
        
        ValueBlock macroObj=(ValueBlock) ret;
        
        List<Value> params=new ArrayList<Value>();
        if (file != null) {
            params.add(new ValueObj(file));
        }
        
        Value result = macroObj.callLambda(ctx.sub(), params);
        ctx.push(result);
    }
    
    @Override
    protected boolean processUnknown (Ctx ctx, File file) throws Exception {
    	if (!this.name.equals("edit")) return false; 
    	
		file.createNewFile();
		ObjFile theFile=new ObjFile(file.getCanonicalPath(), Protection.NoProtection);
		callMacro(ctx, theFile);
		ctx.push(new ValueObj(theFile));
        return true;
    }
    

}
