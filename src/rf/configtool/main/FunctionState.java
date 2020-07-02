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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rf.configtool.main.runtime.Value;

/**
 * The state of a function, lives for the duration of all the program lines and Ctx objects
 * constituting executing a function.
 * 
 * Contains function parameters and assigned variables.
 * 
 * 2020-06-13 Created parent pointers to support macros with parameters inside functions.
 *
 */
public class FunctionState {

    private FunctionState parent;
    private List<Value> params;
    private HashMap<String,Value> assignedVariables=new HashMap<String,Value>();

    public FunctionState() {
        this(new ArrayList<Value>());
    }
    private FunctionState(List<Value> innerParams, FunctionState parent) {
        this.params=innerParams;
        this.parent=parent;
    }
    public FunctionState (List<Value> params) {
        this.params=params;
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
