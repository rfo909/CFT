/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

package rf.configtool.root;

import java.util.List;

import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.Runtime;

public class ScriptState {
    private String scriptName;
    private ObjGlobal objGlobal;
    
    // Default state
    public ScriptState(ObjGlobal objGlobal) throws Exception {
        this(null, objGlobal);
    }

    public ScriptState(String scriptName, ObjGlobal objGlobal) throws Exception {
        this.scriptName = (scriptName == null ? "" : scriptName);

        this.objGlobal = objGlobal;
        if (scriptName != null) {
            objGlobal.loadCode(scriptName);
        }
    }

    public String getScriptName() {
        return scriptName;
    }

    public void updateName (String newName) {
        scriptName=newName;
    }
    
    public ObjGlobal getObjGlobal() {
        return objGlobal;
    }
    
    public Value invokeFunction (Stdio stdio, CFTCallStackFrame caller, String func, List<Value> params) throws Exception {

      // Code lookup
      FunctionBody codeLines=objGlobal.getCurrScriptCode().getFunctionBody(func);
      if (codeLines != null) {
          // execute code line
          Runtime rt=new Runtime(objGlobal);
          Value v = rt.processFunction(stdio, caller, codeLines, new FunctionState(func,params));
          return v;
      }
      
      throw new Exception("Unknown symbol '" + func + "'");
    }


}
