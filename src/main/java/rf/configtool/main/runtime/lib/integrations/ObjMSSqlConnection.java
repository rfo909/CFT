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
            return "callStoredProcedure(name, DictParams) - call stored procedure, returns resultset - nullvalued values are assumed outputs";
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
        		keys.next();
        		if (comma) SQL=SQL+",";
        		SQL=SQL+name;
        	}
        	SQL=SQL+")}";
        	
        	
        	CallableStatement stmt=connection.prepareCall(SQL);
        	
        	// iterate over parameters, adding them to the stmt
        	keys=data.getKeys();
        	while (keys.hasNext()) {
        		String pName=keys.next();
        		MSSqlParam p=getMSSqlParam(pName, data.getValue(pName));
        		p.addParameterToStmt(stmt,pName);
        	}
        	
           	ResultSet resultSet=stmt.executeQuery();

        	// iterate over parameters, processing output values
        	keys=data.getKeys();
        	while (keys.hasNext()) {
        		String pName=keys.next();
        		MSSqlParam p=getMSSqlParam(pName, data.getValue(pName));
        		if (p.isOutput()) {
        			p.setValue(stmt.getObject(pName));
        		}
        	}

        	return new ValueObj(new MSSqlResultSet(resultSet));
        }
    } 
    
    private MSSqlParam getMSSqlParam(String name, Value v) throws Exception {
    	String msg="Invalid parameter '" + name + "': " + v.getTypeName() + " - expected MSSqlParam";
    	if (!(v instanceof ValueObj)) throw new Exception(msg);
    	
		Obj obj=((ValueObj) v).getVal();
		if (!(obj instanceof MSSqlParam)) throw new Exception(msg);
		return (MSSqlParam) obj;
    }
    
    	
       
    
 }
