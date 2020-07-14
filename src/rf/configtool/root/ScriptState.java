package rf.configtool.root;

import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Stdio;

public class ScriptState {
	private String scriptName;
	private ObjGlobal objGlobal;
	private Pipe miso, mosi;

	// Default state
	public ScriptState() throws Exception {
		this(null);
	}

	public ScriptState(String scriptName) throws Exception {
		this.scriptName = (scriptName == null ? "" : scriptName);
		this.miso = new Pipe();
		this.mosi = new Pipe();

		Stdio stdio = new Stdio(mosi.getInputStream(), miso.getOutputStream());
		objGlobal = new ObjGlobal(stdio);
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

	public Pipe getMiso() {
		return miso;
	}

	public Pipe getMosi() {
		return mosi;
	}

}
