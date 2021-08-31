/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main.runtime.lib.integrations;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.security.MessageDigest;
import java.util.*;

import rf.configtool.data.Expr;
import rf.configtool.main.Ctx;
import rf.configtool.main.CtxCloseHook;
import rf.configtool.main.SoftErrorException;
import rf.configtool.main.OutText;
import rf.configtool.main.PropsFile;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDate;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.db2.ObjDb2;

import com.microsoft.sqlserver.jdbc.ISQLServerBulkData;
import com.microsoft.sqlserver.jdbc.ISQLServerBulkRecord;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCSVFileRecord;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopy;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;


import io.sentry.*;
import io.sentry.Sentry.OptionsConfiguration;
import io.sentry.protocol.*;

public class ObjMSSql extends Obj {

	private String user, password, server, database, instance;
	
	
    public ObjMSSql () {
    	this.add(new FunctionSetUser());
    	this.add(new FunctionSetPassword());
    	this.add(new FunctionSetServer());
    	this.add(new FunctionSetDatabase());
    	this.add(new FunctionSetInstance());
    	
        this.add(new FunctionConnect()); 

        this.add(new FunctionIn());
        this.add(new FunctionOut());
        
        this.add(new FunctionSqlTypes());

        
    }
    
    private ObjMSSql self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "MSSql";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("MSSql");
    }
   
    class FunctionSetUser extends Function {
        public String getName() {
            return "setUser";
        }
        public String getShortDesc() {
            return "setUser(str) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected str param");
        	user=getString("str", params, 0);
        	return new ValueObj(self());
        }
    } 
    

    
    
    class FunctionSetPassword extends Function {
        public String getName() {
            return "setPassword";
        }
        public String getShortDesc() {
            return "setPassword(str) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected str param");
        	password=getString("str", params, 0);
        	return new ValueObj(self());
        }
    } 
       
    class FunctionSetServer extends Function {
        public String getName() {
            return "setServer";
        }
        public String getShortDesc() {
            return "setServer(str) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected str param");
        	server=getString("str", params, 0);
        	return new ValueObj(self());
        }
    } 
       
    class FunctionSetDatabase extends Function {
        public String getName() {
            return "setDatabase";
        }
        public String getShortDesc() {
            return "setDatabase(str) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected str param");
        	database=getString("str", params, 0);
        	return new ValueObj(self());
        }
    } 
       
    
    class FunctionSetInstance extends Function {
        public String getName() {
            return "setInstance";
        }
        public String getShortDesc() {
            return "setInstance(str) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected str param");
        	instance=getString("str", params, 0);
        	return new ValueObj(self());
        }
    } 
       
    

    
    class FunctionConnect extends Function {
        public String getName() {
            return "connect";
        }
        public String getShortDesc() {
            return "connect() - returns MSSqlConnection object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no params");

        	SQLServerDataSource ds = new SQLServerDataSource();
        	
            ds.setUser(user);
            ds.setPassword(password);
            ds.setServerName(server);
            if (instance != null) ds.setInstanceName(instance);
            ds.setDatabaseName(database);
            
            String persistKey="ObjMSSql: "+user+":"+server+":"+instance+":"+database;

            Connection connection = ds.getConnection();
            return new ValueObj(new ObjMSSqlConnection(connection, persistKey));

        }
    } 
    
    class FunctionIn extends Function {
        public String getName() {
            return "in";
        }
        public String getShortDesc() {
            return "in(SQLType, value) - returns MSSqlParam input value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 2) throw new Exception("Expected params SQLType (int), value");
        	int sqlType=(int) getInt("SQLType", params, 0);
        	Value value=params.get(1);
        	
        	Object obj;
        	if (value instanceof ValueNull) obj=null;
        	else if (value instanceof ValueString) obj=((ValueString) value).getVal();
        	else if (value instanceof ValueInt) obj=((ValueInt) value).getVal();
        	else {
        		throw new Exception("Unsupported type value " + value.getTypeName());
        	}
        	return new ValueObj(new MSSqlParam(sqlType, obj));
        }
    } 
       

    
    class FunctionOut extends Function {
        public String getName() {
            return "out";
        }
        public String getShortDesc() {
            return "out(SQLType) - returns MSSqlParam output value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected param SQLType (int)");
        	int sqlType=(int) getInt("SQLType", params, 0);
        	
        	return new ValueObj(new MSSqlParam(sqlType));
        }
    } 
       
    
    class FunctionSqlTypes extends Function {
        public String getName() {
            return "sqlTypes";
        }
        public String getShortDesc() {
            return "sqlTypes() - returns Dict mapping names to int values";
        }
        private void set (ObjDict d, int value, String name) {
        	d.set(name, new ValueInt(value));
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no params");
        	ObjDict d=new ObjDict();
        	set(d,Types.VARCHAR, "VARCHAR");
        	set(d,Types.NUMERIC, "NUMERIC");
        	set(d,Types.INTEGER, "INTEGER");
        	set(d,Types.LONGNVARCHAR, "LONGVARCHAR");
        	set(d,Types.TIME, "TIME");
        	set(d,Types.TIMESTAMP, "TIMESTAMP");
        	set(d,Types.NULL, "NULL");
        	return new ValueObj(d);
        }
    } 

    // ---- PUBLIC UTIL 
    
	public static Value mapToValue (Object value) throws Exception {
    	if (value==null) return new ValueNull();
    	if (value instanceof String) return new ValueString((String) value);
    	if (value instanceof Integer) return new ValueInt((Integer) value);
    	if (value instanceof Long) return new ValueInt((Long) value);
    	if (value instanceof java.sql.Timestamp) {
    		java.sql.Timestamp ts=(java.sql.Timestamp)value;
    		ObjDate d=new ObjDate(ts.getTime());
    		return new ValueObj(d);
    	}
    	throw new Exception("Invalid type: " + value.getClass().getName());
	}
	    

       
 
}
