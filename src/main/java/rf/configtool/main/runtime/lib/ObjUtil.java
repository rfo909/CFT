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
import rf.configtool.parsetree.Expr;

public class ObjUtil extends Obj {

    public ObjUtil () {       
        this.add(new FunctionEncrypt());
        this.add(new FunctionDecrypt());
        this.add(new FunctionRandomBinary());
        this.add(new FunctionRandom());
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
            return "Encrypt(passwordBinary, saltString?) - create Encrypt object in encryption mode";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1 && params.size() != 2) throw new Exception("Expected parameter passwordBinary and optional parameter saltString");
            byte[] password=getBinary("passwordBinary",params,0).getVal();
            byte[] salt;
            if (params.size() == 2) {
                salt=getString("saltString",params,1).getBytes("UTF-8");
            } else {
                salt=new byte[0];
            }
            return new ValueObj(new ObjEncrypt(password,salt, true));
        }
    } 
       
   
    class FunctionDecrypt extends Function {
        public String getName() {
            return "Decrypt";
        }
        public String getShortDesc() {
            return "Decrypt(passwordBinary, saltString?) - create Encrypt object in decrypt mode";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1 && params.size() != 2) throw new Exception("Expected parameter passwordBinary and optional parameter saltString");
            byte[] password=getBinary("passwordBinary",params,0).getVal();
            byte[] salt;
            if (params.size() == 2) {
                salt=getString("saltString",params,1).getBytes("UTF-8");
            } else {
                salt=new byte[0];
            }
            return new ValueObj(new ObjEncrypt(password,salt, false));
        }
        
    } 
       
   
    class FunctionRandomBinary extends Function {
        public String getName() {
            return "randomBinary";
        }
        public String getShortDesc() {
            return "randomBinary(seedString) - returns Binary value with 20 random bytes";
        }
        private byte[] create (String pre, Ctx ctx) throws Exception {
            MessageDigest md1 = MessageDigest.getInstance("SHA1"); // 160 bits = 20 bytes
            String s = pre+System.currentTimeMillis();
            md1.update(s.getBytes("UTF-8"));
            md1.update(ctx.getObjGlobal().getRoot().getSecureSessionID());
            return md1.digest();
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected seed string parameter");
            String seed=getString("seedString",params,0);
            return new ValueBinary(create(seed, ctx));
        }
        
    } 
       
    class FunctionRandom extends Function {
        public String getName() {
            return "random";
        }
        public String getShortDesc() {
            return "random() - returns random float x so that 0 <= x < 1";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.random());
        }
        
    } 
       
   


}
