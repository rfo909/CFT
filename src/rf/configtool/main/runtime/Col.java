package rf.configtool.main.runtime;

/**
 * For formatting output. Cols with same names are lined up if possible
 */
public class Col {
    
    public static Col regular(String value) {
        return new Col(value, true);
    }
    public static Col status(String value) {
        return new Col(value, false);
    }
    
    private String value;
    private boolean trunc;
    
    private Col (String value, boolean trunc) {
        this.value=value;
        this.trunc=trunc;
    }
    public String getValue() {
        return value;
    }
    public boolean isTrunc() {
        return trunc;
    }
    
    
}
