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

package rf.configtool.main;

import java.io.File;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;
import rf.configtool.parsetree.ProgramLine;

/**
 * Executing one statement at a time, possibly saving last statement in symbol table.
 */
public class Runtime {
    
    private ObjGlobal objGlobal;
    
    public Runtime (ObjGlobal objGlobal) {
        this.objGlobal=objGlobal;
    }

    /**
     * Returns value from executing program line. Note may return java null if no return
     * value identified
     */
    public Value processCodeLines (Stdio stdio, CodeLines lines, FunctionState functionState) throws Exception {

        if (functionState==null) functionState=new FunctionState();
        
        
        List<ProgramLine> progLines=lines.getProgramLines();

        Value retVal=null;
        
        for (ProgramLine progLine:progLines) {
            Ctx ctx=new Ctx(stdio, objGlobal, functionState);
            if (retVal != null) ctx.push(retVal);
            
            progLine.execute(ctx);
            
            OutText outText=ctx.getOutText();
    
             // Column data is formatted to text and added to outData as String objects
            List<List<Value>> outData=outText.getData();
            Report report=new Report();
            List<String> formattedText=report.formatDataValues(outData);
            for (String s:formattedText) {
                ctx.getOutData().out(new ValueString(s));
            }
            
            retVal=ctx.getResult();
        }
        return retVal;
    }

}
