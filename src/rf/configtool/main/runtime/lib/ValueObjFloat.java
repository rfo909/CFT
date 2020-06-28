package rf.configtool.main.runtime.lib;

import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.*;



/**
 * float value with associated data object, for sorting etc 
 */
public class ValueObjFloat extends ValueFloat {

    private Value data;
    
    public ValueObjFloat(double value, Value data) {
        super(value);
        if (data==null) data=new ValueNull();
        this.data=data;
        
        add(new FunctionData());
    }
    
    private Obj self() {
        return this;
    }
    
    // DO NOT override eq() - need to just compare the value, not the extra data
    
    @Override
    public String synthesize() throws Exception {
        return "Float("+ super.synthesize() + "," + data.synthesize() + ")";
    }


    
    public String getTypeName() {
        return "Float";
    }
    
    public ColList getContentDescription() {
        return ColList.list().status(getTypeName()).status("value: "+getVal()).regular("data: "+data.getValAsString());
    }
    
    class FunctionData extends Function {
        public String getName() {
            return "data";
        }
        public String getShortDesc() {
            return "data() - get data value or null if not defined";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return data;
        }
    }

}
