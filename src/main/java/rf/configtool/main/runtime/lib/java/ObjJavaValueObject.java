package rf.configtool.main.runtime.lib.java;

import rf.configtool.main.runtime.*;

public class ObjJavaValueObject extends ObjJavaValue {
	
	private ObjJavaObject value;
	
	public ObjJavaValueObject (ObjJavaObject value) {
		this.value=value;
	}
	
	@Override
    public Object getAsJavaValue() throws Exception {
		return value.getJavaObject();
	}
	@Override
  	public Value getAsCFTValue() throws Exception {
		if (value==null) return new ValueNull();
		return new ValueObj(value);
		
	}


}
