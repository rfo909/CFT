package rf.configtool.main;

import rf.configtool.lexer.SourceLocation;

/**
 * Exception related to CFT code
 *
 */
public abstract class CodeException extends CustomException {
	
    private SourceLocation loc;
    private String msg;
    private Exception originalException;
    
    public CodeException (SourceLocation loc, String msg, Exception originalException ) {
        super(msg);
        this.loc=loc;
        this.msg=msg;
        this.originalException=originalException;
    }
    
    public CodeException (SourceLocation loc, String msg) {
        this(loc,msg,null);
    }

    public CodeException (SourceLocation loc, Exception originalException) {
        this(loc,originalException.getMessage(),originalException);
    }

    public SourceLocation getLoc() {
        return loc;
    }

    @Override
    public String getMessage() {
        String s=(loc != null ? loc.toString() + " " : "") + msg;
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
