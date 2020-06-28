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
 * Load savefile into separate context, look up a named function, and execute it with given parameters,
 * including a data override for named functions within the scope. The data overrides replaces named
 * functions with values. When code inside the script calls those, note that parameters are ignored.
 */
public class ExternalScriptState {


    private ObjGlobal objGlobal;
    private String script;
    
    public ExternalScriptState (Stdio stdio, String script) throws Exception {
        this.script=script;
        objGlobal=new ObjGlobal(stdio);
        objGlobal.loadCode(script);
    }

    public Value invokeFunction (String func, FuncOverrides funcOverrides, List<Value> params) throws Exception {

        // Code lookup
    	objGlobal.setFuncOverrides(funcOverrides);
    	
        CodeLines codeLines=objGlobal.getCodeHistory().getNamedLine(func);
        if (codeLines != null) {
            // execute code line
            Runtime rt=new Runtime(objGlobal);
            Value v = rt.processCodeLines(codeLines, new FunctionState(params));
            objGlobal.clearFuncOverrides();
            return v;
        }
        
        throw new Exception("Unknown symbol '" + func + "'");
    }

}
