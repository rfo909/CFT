package rf.configtool.main;

/**
 * Soft exception thrown by global function error() - caught by tryCatchSoft() as well as tryCatch()
 * which catches soft and hard errors.
 */
public class SoftException extends Exception{
	
	public SoftException (String msg) {
		super(msg);
	}
	
	

}
