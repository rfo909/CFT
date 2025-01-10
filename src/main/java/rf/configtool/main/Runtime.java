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

package rf.configtool.main;

import java.util.List;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parsetree.CodeSpace;

/**
 * Executing function body one CodeSpace at a time, managing transfer of result from one
 * code space to data stack of next, or if last code space, function return value.
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
    public Value processFunction (Stdio stdio, CFTCallStackFrame caller, FunctionBody functionBody, FunctionState functionState) throws Exception {

        stdio.pushCFTCallStackFrame(caller);

        if (functionState == null) throw new Exception("No functionState");

        ClassDetails cd=functionBody.getClassDetails();
        
        if (cd != null) {
            String typeName=cd.getType();
            ObjDict self=new ObjDict(typeName);
            functionState.set("self", new ValueObj(self));
            doProcessFunction(stdio, caller, functionBody, functionState);
            return new ValueObj(self);
        } else {
            return doProcessFunction(stdio,caller, functionBody, functionState);
        }
        
    }
     
    private Value doProcessFunction(Stdio stdio, CFTCallStackFrame caller, FunctionBody functionBody, FunctionState functionState) throws Exception {
        List<CodeSpace> codeSpaces=functionBody.getCodeSpaces();

        Value retVal=null;
        
        for (CodeSpace codeSpace:codeSpaces) {
            Ctx ctx=new Ctx(stdio, objGlobal, functionState);
            
            if (retVal != null) ctx.push(retVal);
            
            codeSpace.execute(ctx);

            retVal=ctx.getResult();
        }
        
        stdio.popCFTCallStackFrame(caller);
        return retVal;
    }

}
