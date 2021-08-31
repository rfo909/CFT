package rf.configtool.main.runtime.lib.integrations;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;

import com.microsoft.sqlserver.jdbc.ISQLServerBulkData;
import com.microsoft.sqlserver.jdbc.ISQLServerBulkRecord;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCSVFileRecord;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopy;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;


public class ObjMSSqlConnection extends Obj {
	
	private Connection connection;
	private String desc;
	
    public ObjMSSqlConnection (Connection connection, String desc) {
    	this.connection=connection;
    	this.desc=desc;
    	
        this.add(new FunctionCallStoredProcedure());        
        
    }
    
    private ObjMSSqlConnection self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "MSSqlConnection";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("MSSqlConnection");
    }
    

    
    class FunctionCallStoredProcedure extends Function {
        public String getName() {
            return "callStoredProcedure";
        }
        public String getShortDesc() {
            return "callStoredProcedure(name, DictParams) - call stored procedure, returns resultset";
        }
        private void TELL (String s) {
        	System.out.println("TELL: " + s);
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 2) throw new Exception("Expected parameters name, DictParams");
        	String name=getString("name", params, 0);
        	ObjDict data=(ObjDict) getObj("DictParams", params, 1);
        	
        	// iterate over parameters, to create SQL statement
        	String SQL="{call " + name + "(";
        	Iterator<String> keys=data.getKeys();
        	boolean comma=false;
        	
        	while (keys.hasNext()) {
        		String pName=keys.next(); 
        		if (comma) SQL=SQL+", ";
        		//SQL=SQL+"@"+pName+"=?";
        		//SQL=SQL+pName;
        		SQL=SQL+"?";
        		comma=true;
        	}
        	SQL=SQL+")}";
        	
        	TELL("callStoredProcedure: SQL=" + SQL);
        	
        	CallableStatement stmt=connection.prepareCall(SQL);
        	
        	TELL("callStoredProcedure: CallableStatement ok");
        	
        	// iterate over parameters, adding them to the stmt
        	keys=data.getKeys();
        	int pos=1;  // 1-based!
        	while (keys.hasNext()) {
        		String pName=keys.next();
        		ObjMSSqlParam p=getMSSqlParam(pName, data.getValue(pName));
        		p.addParameterToStmt(stmt, pos);
        		pos++;
        	}
        	
        	TELL("Parameter values added ok");
        	
           	ResultSet resultSet=stmt.executeQuery();
           	
           	TELL("After executeQuery");

        	// iterate over parameters, processing output values
        	keys=data.getKeys();
        	pos=1;
        	while (keys.hasNext()) {
        		String pName=keys.next();
        		ObjMSSqlParam p=getMSSqlParam(pName, data.getValue(pName)); 
        		if (p.isOutput()) {
        			p.setValue(stmt.getObject(pos));
        		}
        		pos++;
        	}
        	
        	TELL("After processing output values");

        	return new ValueObj(new ObjMSSqlResultSet(resultSet));
        }
    } 
    
    private ObjMSSqlParam getMSSqlParam(String name, Value v) throws Exception {
    	String msg="Invalid parameter '" + name + "': " + v.getTypeName() + " - expected MSSqlParam";
    	if (!(v instanceof ValueObj)) throw new Exception(msg);
    	
		Obj obj=((ValueObj) v).getVal();
		if (!(obj instanceof ObjMSSqlParam)) throw new Exception(msg);
		return (ObjMSSqlParam) obj;
    }
    
    	
       
    
 }
