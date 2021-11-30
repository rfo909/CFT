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
import rf.configtool.main.runtime.lib.db.ObjDb2;
import rf.configtool.parsetree.Expr;
import rf.configtool.util.Encrypt;
import rf.configtool.util.Hex;

public class ObjEncrypt extends Obj {
    
    private Encrypt encrypt;
    private boolean modeEncrypt;

    public ObjEncrypt (byte[] password, byte[] salt, boolean modeEncrypt) throws Exception {
        encrypt=new Encrypt(password, salt);
        this.modeEncrypt=modeEncrypt;
        
        this.add(new FunctionProcess());
        this.add(new FunctionProcessString());
    }
    
    private ObjEncrypt self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "Encrypt";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Encrypt");
    }
   
    class FunctionProcess extends Function {
        public String getName() {
            return "process";
        }
        public String getShortDesc() {
            return "process(binary) - returns binary of same size";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected binary value parameter");
            ValueBinary binary=getBinary("binary",params,0);
            
            byte[] data=binary.getVal();
            byte[] result=new byte[data.length];
            for (int i=0; i<data.length; i++) result[i]=encrypt.process(modeEncrypt, data[i]);
            return new ValueBinary(result);
        }
    } 

 
    class FunctionProcessString extends Function {
        public String getName() {
            return "processString";
        }
        public String getShortDesc() {
            return "processString(value) - returns string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter value");
            String str=getString("value", params, 0);
            byte[] data=modeEncrypt ? str.getBytes("UTF-8") : Hex.fromHex(str);
            byte[] result=new byte[data.length];
            for (int i=0; i<data.length; i++) result[i]=encrypt.process(modeEncrypt, data[i]);
            return new ValueString(
                    modeEncrypt ? Hex.toHex(result) : new String(result,"UTF-8")
            );
        }
    } 

 
}
