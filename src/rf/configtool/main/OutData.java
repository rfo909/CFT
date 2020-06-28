package rf.configtool.main;

import java.util.*;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.reporttool.Report;

/**
 * Data from StmtOut
 */
public class OutData {
    
    private boolean programContainsLooping=false; 
    private List<Value> outData=new ArrayList<Value>();

    /**
     * If a program line contains looping, then the Runtime should call
     * getOutData() to produce a result value, regardless of value on stack
     */
    public void setProgramContainsLooping () {
        this.programContainsLooping=true;
    }
    
    public boolean programContainsLooping() {
        return programContainsLooping;
    }
    
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
