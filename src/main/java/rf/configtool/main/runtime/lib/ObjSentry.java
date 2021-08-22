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

package rf.configtool.main.runtime.lib;

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
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.db2.ObjDb2;

import io.sentry.Sentry;

public class ObjSentry extends Obj {

	private boolean initOk=false;
	private String sessionKey="sessionKey";
	
    public ObjSentry () {       
        this.add(new FunctionInit());
        this.add(new FunctionSetSessionKey());
        this.add(new FunctionException());
    }
    
    private ObjSentry self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "Sentry";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Sentry");
    }
   
    class FunctionInit extends Function {
        public String getName() {
            return "init";
        }
        public String getShortDesc() {
            return "init(DSNString) - initialize Sentry with Data Source Name string - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	String dsn=getString("DSNString", params, 0);
        	
        	Sentry.init(dsn);
        	initOk=true;
        	return new ValueObj(self());
        }
    } 
       
    
    class FunctionSetSessionKey extends Function {
        public String getName() {
            return "setSessionKey";
        }
        public String getShortDesc() {
            return "setSessionKey(sessionKey) - sets internal state used when logging - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	sessionKey=getString("sessionKey", params, 0);
        	return new ValueObj(self());
        }
    } 
  
   
    class FunctionException extends Function {
        public String getName() {
            return "exception";
        }
        public String getShortDesc() {
            return "exception(msg) - log exception with Sentry - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (!initOk) throw new Exception("Must call init() first");
        	String msg=getString("msg", params, 0);
        	try {
        		throw new Exception(msg);
        	} catch (Exception ex) {
        		Sentry.setTag("sessionKey", sessionKey);
        		Sentry.captureException(ex);
        	}
        	return new ValueObj(self());
        }
    } 


}
