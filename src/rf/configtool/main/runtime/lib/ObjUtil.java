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
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.db2.ObjDb2;

public class ObjUtil extends Obj {

    public ObjUtil () {    	
        this.add(new FunctionEncrypt());
        this.add(new FunctionDecrypt());
    }
    
    private ObjUtil self() {
    	return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "Util";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Util");
    }
   
    class FunctionEncrypt extends Function {
        public String getName() {
            return "Encrypt";
        }
        public String getShortDesc() {
            return "Encrypt(password, salt) - create Encrypt object in encryption mode";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected string parameters password + salt");
            byte[] password=getString("password",params,0).getBytes("UTF-8");
            byte[] salt=getString("password",params,1).getBytes("UTF-8");
            return new ValueObj(new ObjEncrypt(password,salt, true));
        }
    } 
       
   
    class FunctionDecrypt extends Function {
        public String getName() {
            return "Decrypt";
        }
        public String getShortDesc() {
            return "Decrypt(passwordStr, saltStr) - create Encrypt object in decrypt mode";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected string parameters password + salt");
            byte[] password=getString("password",params,0).getBytes("UTF-8");
            byte[] salt=getString("password",params,1).getBytes("UTF-8");
            return new ValueObj(new ObjEncrypt(password,salt, false));
        }
        
    } 
       
   
 
}
