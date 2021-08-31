package rf.configtool.main.runtime.lib.integrations;

import java.sql.ResultSet;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;


public class MSSqlResultSet extends Obj {

	private ResultSet resultSet; 
	private int rowNumber=0;
	
    public MSSqlResultSet (ResultSet resultSet) {
    	this.resultSet=resultSet;
    	
    	add(new FunctionNext());
    	add(new FunctionGet());
    }
    
    private MSSqlResultSet self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "MSSqlResultSet";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("MSSqlResultSet");
    }
   
    class FunctionNext extends Function {
        public String getName() {
            return "next";
        }
        public String getShortDesc() {
            return "next() - advance to first or next row, return false if no more rows";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no params");
        	return new ValueBoolean(resultSet.next());
        }
    }
    

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get(colPosOrName) - returns column value for current row";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected parameter colPosOrName");
        	Value intOrString=params.get(0);
        	
        	Object obj;
        	if (intOrString instanceof ValueInt) {
        		obj=resultSet.getObject( (int) ((ValueInt) intOrString).getVal() );
        	} else if (intOrString instanceof ValueString) {
        		obj=resultSet.getObject( ((ValueString) intOrString).getVal() );
        	} else {
        		throw new Exception("Expected parameter colPosOrName to be int or string");
        	}
        	return ObjMSSql.mapToValue(obj);
        }
    }
   
}