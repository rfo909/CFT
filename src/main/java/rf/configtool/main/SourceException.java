package rf.configtool.main;

import rf.configtool.lexer.SourceLocation;

/**
 * Exceptions arising in own code, which can relate to a source location, should throw this. In addition, 
 * operations that may fail inside Java runtime / libs, should capture these and repackage them into
 * SourceExceptions at the earliest convenient time, ensuring proper reporting to user.
 */
public class SourceException extends Exception {
    
    private SourceLocation loc;
    private String msg;
    private Exception originalException;
    
    public SourceException (SourceLocation loc, String msg, Exception originalException ) {
        super(msg);
        this.loc=loc;
        this.msg=msg;
        this.originalException=originalException;
    }
    
    public SourceException (SourceLocation loc, String msg) {
        this(loc,msg,null);
    }

    public SourceException (SourceLocation loc, Exception originalException) {
        this(loc,originalException.getMessage(),originalException);
    }

    public SourceLocation getLoc() {
        return loc;
    }

    @Override
    public String getMessage() {
        String s=loc.toString() + " " + msg;
        if (originalException != null) s+=" (" + originalException.getClass().getName() + ")";
        return s;
    }

    public Exception getOriginalException() {
        return originalException;
    }
    
    
    public String toString() {
        return getMessage();
    }
    
    

}
