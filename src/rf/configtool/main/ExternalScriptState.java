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

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parser.TokenStream;

/**
 * When calling external scripts, we need to persist the ObjGlobal.
 * 
 * Load savefile into separate context, look up a named function, and execute it with given parameters.
 */
public class ExternalScriptState {


    private ObjGlobal objGlobal;
    private String script;
    
    public ExternalScriptState (Stdio stdio, String script) throws Exception {
        this.script=script;
        objGlobal=new ObjGlobal(stdio);
        objGlobal.loadCode(script);
    }

    public Value invokeFunction (String func, List<Value> params) throws Exception {

        // Code lookup
        CodeLines codeLines=objGlobal.getCodeHistory().getNamedLine(func);
        if (codeLines != null) {
            // execute code line
            Runtime rt=new Runtime(objGlobal);
            Value v = rt.processCodeLines(codeLines, new FunctionState(params));
            return v;
        }
        
        throw new Exception("Unknown symbol '" + func + "'");
    }

}
