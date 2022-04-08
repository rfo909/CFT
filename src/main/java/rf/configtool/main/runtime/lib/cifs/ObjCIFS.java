package rf.configtool.main.runtime.lib.cifs;

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
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

public class ObjCIFS extends Obj {

	
    public ObjCIFS () {
    	this.add(new FunctionCIFSFile());
    	this.add(new FunctionCreateURL());
    	this.add(new FunctionCIFSContext());
    	this.add(new Function_Example());
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
            return "CIFSFile(url [, CIFSContext]) - create CIFSFile object from url starting with smb://";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	String url=null;
        	CIFSContext context=null;
            if (params.size() >= 1) {
            	url=getString("url",params,0);
            } 
            if (params.size() >= 2) {
            	Obj obj=getObj("CIFCContext",params,1);
            	context=((ObjCIFSContext) obj).getContext();
            }
            if (url==null) throw new Exception("Expected parameters url [, CIFSContext]");
            
            if (!url.startsWith("smb://")) throw new Exception("Invalid url, should start with smb:// - got " + url);
            return new ValueObj(new ObjCIFSFile(url,context));
        }
    } 
    
    class FunctionCreateURL extends Function {
        public String getName() {
            return "createURL";
        }
        public String getShortDesc() {
            return "createURL(domain?,username?,password?,hostport,path) - returns smb://... string";
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
            
            
            String url = createSmbUrl(domain, username, password, hostport, path);
            return new ValueString(url);
        }
    }            


    class FunctionCIFSContext extends Function {
        public String getName() {
            return "CIFSContext";
        }
        public String getShortDesc() {
            return "CIFSContext(domain?, username, password) - returns CIFSContext object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 3) throw new Exception("Expected parameters: domain?,username,password");
            
            String domain,username,password;
            
            domain=username=password=null;
            
            if (!(params.get(0) instanceof ValueNull)) domain=getString("domain",params,0);
            username=getString("username",params,1);
            password=getString("password",params,2);

            CIFSContext x=createCIFSContext(domain, username, password);
            return new ValueObj(new ObjCIFSContext(x));
        }
    }            

    
    class Function_Example extends Function {
        public String getName() {
            return "_Example";
        }
        public String getShortDesc() {
            return "_Example() - display information about use";
        }
        private String[] data= {
            "",
            "Method 1: put all data into URL",
            "-------------------------------",
            "",
            "domain=null",
            "user='xxx'",
            "password='yyy'",
            "url=Lib.Util.CIFS.createURL(domain,user,password,'host','/share/path/file.txt')",
            "file=Lib.Util.CIFS.CIFSFile(url)",
            "",
            "This method uses deprecated constructor for SmbFile() in ObjCIFSFile java class.",
            "",
            "",
            "Method 2: use CIFSContext",
            "-------------------------",
            "",
            "domain=null",
            "user='xxx'",
            "password='yyy'",
            "url='smb://host/share/path/file.txt'",
            "context=Lib.Util.CIFS.CIFSContext(domain,user,password)",
            "file=Lib.Util.CIFS.CIFSFile(url,context)",
            "",
            "This does NOT use deprecated code :-)",
            "It also makes it safe to log the url, as it contains no secrets",
            "",
        };
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (String line:data) {
                ctx.getObjGlobal().addSystemMessage(line);
            }
            return new ValueBoolean(true);
        }
        
        
    } 
    
    
	// -------------------------
	// First implementation: embed all information into URL and create SmbFile from it alone.
	//    new SmbFile(url) // deprecated
	// -------------------------
	
	public static String createSmbUrl (String domain, String username, String password, String hostPort, String path) {
		if (domain==null) domain=""; else domain=domain+";";
		if (username==null) username="";
		if (password==null) password=""; else password=":"+password;
		
		path=path.replace('\\', '/');
		if (!path.startsWith("/")) path="/"+path;

		if((username+password).length() > 0) password=password+"@";
		
		return "smb://"+domain+username+password+hostPort+path;
	}
	

	// -------------------------------------------------------------------------
	// Second implementation
	// -------------------------------------------------------------------------
	
	public static CIFSContext createCIFSContext (String domain, String username, String password) throws Exception {
		NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(domain, username, password);
		Properties p=new Properties();
		
//        p.setProperty("jcifs.smb.client.soTimeout", "300000");
//        p.setProperty("jcifs.netbios.cachePolicy", "600");

		PropertyConfiguration conf = new PropertyConfiguration(p);
		CIFSContext cifsContext = new BaseContext(conf).withCredentials(auth);
		return cifsContext;
	}

	public static SmbFile getSmbFile (String url, CIFSContext context) throws Exception {
		return new SmbFile(url, context);
	}    

}
