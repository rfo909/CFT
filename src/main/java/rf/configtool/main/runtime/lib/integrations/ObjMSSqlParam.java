package rf.configtool.main.runtime.lib.integrations;

import java.sql.CallableStatement;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

public class ObjMSSqlParam extends Obj {

	private int sqlType;
	private boolean output;
	private Object value;

	private void init() {
		this.add(new FunctionIsOutput());
		this.add(new FunctionGetValue());
	}
	
    public ObjMSSqlParam(int sqlType, Object value) {
		super();
		this.sqlType = sqlType;
		this.output = false;
		this.value = value;
		
		init();
	}

    
    public ObjMSSqlParam(int sqlType) {
		super();
		this.sqlType = sqlType;
		this.output = true;
		this.value = null;
		
		init();
	}
    
    public void addParameterToStmt (CallableStatement stmt, int pos) throws Exception {
		if (output) {
			stmt.registerOutParameter(pos, sqlType);
		} else {
			stmt.setObject(pos, value, sqlType);
		}

    }

    private ObjMSSqlParam self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "MSSqlParam";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list()
        		.regular("MSSqlParam")
        		.regular("sqlType=" + sqlType)
        		.regular(output?"output":"input")
        		.regular(value==null?"null":value.toString());
    }


	public int getSqlType() {
		return sqlType;
	}


	public boolean isOutput() {
		return output;
	}


	public Object getValue() {
		return value;
	}
	
	public void setValue (Object value) {
		this.value=value;
	}
	
	
	// --- CFT functions ---
	
	class FunctionIsOutput extends Function {
        public String getName() {
            return "isOutput";
        }
        public String getShortDesc() {
            return "isOutput() - returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no params");
        	return new ValueBoolean(output);
        }
    } 
       

	class FunctionGetValue extends Function {
        public String getName() {
            return "getValue";
        }
        public String getShortDesc() {
            return "getValue() - returns value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no params");
        	return ObjMSSql.mapToValue(value);
        }
    } 
       
	
   
}