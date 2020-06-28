package rf.configtool.main.runtime;

import java.util.ArrayList;
import java.util.List;

public abstract class Value extends Obj {
    
    public abstract String getTypeName();
    
    public ColList getContentDescription() {
        return ColList.list().regular(getValAsString());
    }
    public abstract String getValAsString();
    
    public abstract boolean eq(Obj v);
    
    /**
     * boolean false, null and empty list are false, all other values are true
     */
    public abstract boolean getValAsBoolean();

    
}
