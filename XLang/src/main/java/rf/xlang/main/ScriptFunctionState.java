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

import java.util.HashMap;
import java.util.List;

import rf.xlang.main.runtime.Value;
import rf.xlang.main.runtime.ValueNull;
import rf.xlang.parsetree.CodeFunction;

public class ScriptFunctionState {

    private CodeFunction function;

    private HashMap<String,Value> assignedVariables=new HashMap<String,Value>();

    public ScriptFunctionState(CodeFunction function, List<Value> params) {
        this.function=function;

        // store parameter values as assigned variables according to the CodeFunction definition
        int pos=0;
        for (String paramName : function.getParameters()) {
            if (pos < params.size()) {
                assignedVariables.put(paramName, params.get(pos));
            } else {
                assignedVariables.put(paramName, new ValueNull());
            }
            pos++;
        }
    }
    
    public CodeFunction getFunction() {
        return function;
    }

    private boolean findAndSet (String varName, Value value) {
        if (assignedVariables.get(varName) != null) {
            assignedVariables.put(varName, value);
            return true;
        }
        return false;
    }
    
    public void set(String varName, Value value) {
        if (value==null) throw new RuntimeException("Invalid value: null");
        if (!findAndSet(varName,value)) {
            assignedVariables.put(varName, value);
        }
        //System.out.println("ScriptFunctionState: setting: "+varName+"=" + value.getValAsString());
    }

    public Value get(String varName) {
        return assignedVariables.get(varName);  // null if not found
    }
}
