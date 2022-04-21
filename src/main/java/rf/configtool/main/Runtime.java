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

import java.util.List;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;
import rf.configtool.parsetree.ProgramCodeSpace;

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
    public Value processCodeLines (Stdio stdio, CFTCallStackFrame caller, FunctionCodeLines lines, FunctionState functionState) throws Exception {

    	stdio.pushCFTCallStackFrame(caller);

        if (functionState == null) throw new Exception("No functionState");
        
        
        List<ProgramCodeSpace> codeSpaces=lines.getCodeSpaces();

        Value retVal=null;
        
        for (ProgramCodeSpace codeSpace:codeSpaces) {
            Ctx ctx=new Ctx(stdio, objGlobal, functionState);
            
            if (retVal != null) ctx.push(retVal);
            
            codeSpace.execute(ctx);
            
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
        
        stdio.popCFTCallStackFrame(caller);
        return retVal;
    }

}
