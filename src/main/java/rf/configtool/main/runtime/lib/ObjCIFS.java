package rf.configtool.main.runtime.lib;

/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

import java.security.MessageDigest;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.util.CIFS;

public class ObjCIFS extends Obj {

	
    public ObjCIFS () {
    	this.add(new FunctionCIFSFile());
    	this.add(new FunctionCreateURL());
}
    
    private ObjCIFS self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "CIFS";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("CIFS");
    }
   
    class FunctionCIFSFile extends Function {
        public String getName() {
            return "CIFSFile";
        }
        public String getShortDesc() {
            return "CIFSFile(url) - create CIFSFile object from url starting with smb://";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected url parameter");
            String url=getString("url",params,0);
            if (!url.startsWith("smb://")) throw new Exception("Invalid CIFS url, should start with smb:// - got " + url);
            return new ValueObj(new ObjCIFSFile(url));
        }
    } 
    
    class FunctionCreateURL extends Function {
        public String getName() {
            return "createURL";
        }
        public String getShortDesc() {
            return "createURL(domain?,username?,password?,hostport,path) - returns smb:// url";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 5) throw new Exception("Expected parameters: domain?,username?,password?,hostport,path");
            
            String domain,username,password,hostport,path;
            
            domain=username=password=hostport=path=null;
            
            if (!(params.get(0) instanceof ValueNull)) domain=getString("domain",params,0);
            if (!(params.get(1) instanceof ValueNull)) username=getString("username",params,1);
            if (!(params.get(2) instanceof ValueNull)) password=getString("password",params,2);
            
            hostport=getString("hostport",params,3);
            path=getString("path",params,4);
            
            
            String url = CIFS.createSmbUrl(domain, username, password, hostport, path);
            return new ValueString(url);
        }
    }            



}
