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

package rf.configtool.parsetree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.CodeLines;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;

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

        if (name.equals("cat")) {
            macro=propsFile.getMCat();
        } else if (name.equals("edit")) {
            macro=propsFile.getMEdit(); 
        } else if (name.equals("more")) {
            macro=propsFile.getMMore();
        } else {
            throw new Exception("Invalid statement name, expected edit or more: " + name);
        }

        //OutText out = ctx.getOutText();
        //out.addSystemMessage("Running " + PropsFile.PROPS_FILE + "." + fieldName + " macro: " + macro);
        
        CodeLines codeLines=new CodeLines(macro, loc);
        
    	CFTCallStackFrame caller=new CFTCallStackFrame("Lambda for " + name);

        Value ret = ctx.getObjGlobal().getRuntime().processCodeLines(ctx.getStdio(), caller, codeLines, new FunctionState(null,null));
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
    	return false;
    }
    

}
