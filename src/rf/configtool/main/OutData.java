package rf.configtool.main;

import java.util.*;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.reporttool.Report;

/**
 * Data from StmtOut
 */
public class OutData {
    
    private List<Value> outData=new ArrayList<Value>();

    public void out (Value value) {
        outData.add(value);
    }
    
    public List<Value> getOutData() {
        return outData;
    }
    
    public boolean isEmpty() {
        return outData.isEmpty();
    }
    

}
