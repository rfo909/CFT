package rf.configtool.main.runtime.lib.java;

import rf.configtool.main.runtime.*;

public class ObjJavaValueString extends ObjJavaValue {
	
	private String value;
	
	public ObjJavaValueString (String value) {
		this.value=value;
	}
	
	@Override
    public Object getAsJavaValue() throws Exception {
		return value;
	}
	@Override
  	public Value getAsCFTValue() throws Exception {
		if (value==null) return new ValueNull();
		return new ValueString(value);
	}


}
