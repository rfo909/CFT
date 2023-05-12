package rf.configtool.main;

import rf.configtool.main.runtime.Function;

public class FunctionException extends CustomException {
	
	public FunctionException (Function function, String msg) {
		super(function.getName() + ": " + msg);
	}

}
