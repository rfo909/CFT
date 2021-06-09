package rf.configtool.main;

/**
 * Soft exception thrown by global function error() - caught by tryCatchSoft() as well as tryCatch()
 * which catches soft and hard errors.
 */
public class SoftErrorException extends Exception{
    
    public SoftErrorException (String msg) {
        super(msg);
    }
    
    

}
