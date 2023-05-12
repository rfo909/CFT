package rf.configtool.main;

public abstract class CustomException extends Exception {
	
	public CustomException (String msg) {
		super(msg);
	}
	
	public CustomException (String msg, Exception ex) {
		super(msg,ex);
	}

}
