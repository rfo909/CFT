package rf.configtool.root;

import java.util.List;

import rf.configtool.main.CodeLines;
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
	
	public Value invokeFunction (Stdio stdio, String func, List<Value> params) throws Exception {
	
	  // Code lookup
	  CodeLines codeLines=objGlobal.getCodeHistory().getNamedCodeLines(func);
	  if (codeLines != null) {
	      // execute code line
	      Runtime rt=new Runtime(objGlobal);
	      Value v = rt.processCodeLines(stdio, codeLines, new FunctionState(params));
	      return v;
	  }
	  
	  throw new Exception("Unknown symbol '" + func + "'");
	}


}
