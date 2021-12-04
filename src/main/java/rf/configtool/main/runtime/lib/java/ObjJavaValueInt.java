package rf.configtool.main.runtime.lib.java;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueNull;

public class ObjJavaValueInt extends ObjJavaValue {
	
	private Integer value;
	
	public ObjJavaValueInt (Integer value) {
		this.value=value;
	}
	
	@Override
    public Object getAsJavaValue() throws Exception {
		return value;
	}
	@Override
  	public Value getAsCFTValue() throws Exception {
		if (value==null) return new ValueNull();
		return new ValueInt(value);
	}


}
