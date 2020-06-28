package rf.configtool.main.runtime;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;

public abstract class Function {
    
    public abstract String getName();
    public abstract String getShortDesc();
    
    public abstract Value callFunction (Ctx ctx, List<Value> params) throws Exception;

}
