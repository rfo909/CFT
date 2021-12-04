package rf.configtool.main.runtime.lib.java;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueNull;

public class ObjJavaValueBoolean extends ObjJavaValue {
	
	private Boolean value;
	
	public ObjJavaValueBoolean (Boolean value) {
		this.value=value;
	}
	
	@Override
    public Object getAsJavaValue() throws Exception {
		return value;
	}
	@Override
  	public Value getAsCFTValue() throws Exception {
		if (value==null) return new ValueNull();
		return new ValueBoolean(value);
	}


}
