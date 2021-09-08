package rf.configtool.main.runtime.lib.java;

import rf.configtool.main.runtime.*;

public class ObjJavaValueNull extends ObjJavaValue {
	
	public ObjJavaValueNull () {
	}
	
	@Override
    public Object getAsJavaValue() throws Exception {
		return null;
	}
	@Override
  	public Value getAsCFTValue() throws Exception {
		return new ValueNull();
	}


}
