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

package rf.xlang.main;

import java.util.List;

import rf.xlang.lexer.SourceLocation;
import rf.xlang.main.runtime.Function;
import rf.xlang.main.runtime.Value;

/**
 * Runtime context for executing code
 */
public class Ctx {

    private Ctx parent;

    private ScriptFunctionState scriptFunctionState;

    private ObjGlobal objGlobal;

    private String loopVariableName;
    private Value loopVariableValue;

    private boolean continueIterationFlag; // "continue"
    private boolean breakLoopFlag;  // "break"
    private Value functionReturnValue = null; // return expr


    /**
     * Root Ctx object, before invoking any function
     */
    public Ctx(ObjGlobal objGlobal) {
        this.parent = null;
        this.objGlobal = objGlobal;
        this.scriptFunctionState = null;
    }

    private Ctx(Ctx parent, ObjGlobal objGlobal, ScriptFunctionState scriptFunctionState) {
        this.parent = parent;
        this.objGlobal = objGlobal;
        this.scriptFunctionState = scriptFunctionState;

    }

    public Ctx sub(ScriptFunctionState scriptFunctionState) {
        return new Ctx(this, this.objGlobal, scriptFunctionState);
    }

    public Ctx sub() {
        return new Ctx(this, this.objGlobal, this.scriptFunctionState);
    }

    public void setLoopVariable(String name, Value value) {
        loopVariableName = name;
        loopVariableValue = value;
    }

    public boolean checkLoopVariable(String name) {
        return loopVariableName != null && loopVariableName.equals(name);
    }

    public Value getLoopVariableValue() {
        return loopVariableValue;
    }

    public Value getVariable (String name) {
        return scriptFunctionState.get(name);
    }

    public void setVariable (String name, Value value) {
        scriptFunctionState.set(name,value);
    }
    public ObjGlobal getObjGlobal() {
        return objGlobal;
    }
    
    public ScriptFunctionState getScriptFunctionState() {
        return scriptFunctionState;
    }
    
    
    public void setContinueIterationFlag() {
        continueIterationFlag=true;
    }
    
    public void setBreakLoopFlag() {
        breakLoopFlag=true;
    }

    public void setFunctionReturnValue (Value v) {
        functionReturnValue=v;
    }
    public Value getFunctionReturnValue() {
        return functionReturnValue;
    }
    
    public boolean hasContinueIterationFlag() {
        return continueIterationFlag;
    }
    
    public boolean hasBreakLoopFlag() {
        return breakLoopFlag;
    }


    private Value callMemberFunction(SourceLocation loc, Function f, List<Value> parameters) throws Exception {
        try {
            return f.callFunction(this, parameters);
        } catch (Exception ex) {
            throw new SourceException(loc, ex);
        }
    }
}
