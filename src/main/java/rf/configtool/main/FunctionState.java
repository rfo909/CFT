/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.main.runtime.Value;

/**
 * The state of a function, lives for the duration of all the program lines and Ctx objects
 * constituting executing a function.
 * 
 * Contains function parameters and assigned variables.
 * 
 */
public class FunctionState {

    private String scriptFunctionName;  
        // Used by Sys.currFunction
        // Note: FunctionState objects (via parent) do not provide a full stack of calls; they are
        // only stacked (via parent pointer) for sub-scopes within functions.
    
    private FunctionState parent;
    private List<Value> params;
    private HashMap<String,Value> assignedVariables=new HashMap<String,Value>();

    public FunctionState(String scriptFunctionName) {
        this(scriptFunctionName, new ArrayList<Value>());
    }
    private FunctionState(List<Value> innerParams, FunctionState parent) {
        this.params=innerParams;
        this.parent=parent;
    }
    public FunctionState (String scriptFunctionName, List<Value> params) {
        this.scriptFunctionName=scriptFunctionName;
        if (params==null) params=new ArrayList<Value>();
        this.params=params;
    }
    
    public String getScriptFunctionName() {
        if (scriptFunctionName != null) return scriptFunctionName;
        if (parent != null) return parent.getScriptFunctionName();
        return null;
    }
    
    public List<Value> getParams() {
        return params;
    }
    
    private boolean findAndSet (String varName, Value value) {
        if (assignedVariables.get(varName) != null) {
            assignedVariables.put(varName, value);
            return true;
        }
        if (parent != null) return parent.findAndSet(varName, value);
        return false;
    }
    
    public void set(String varName, Value value) {
        if (!findAndSet(varName,value)) {
            assignedVariables.put(varName, value);
        }
    }
    public Value get(String varName) {
        Value v=assignedVariables.get(varName);
        if (v != null) return v;
        if (parent != null) return parent.get(varName);
        return null;
    }
    
    public FunctionState sub(List<Value> innerParams) {
        return new FunctionState(innerParams, this);
    }
}
