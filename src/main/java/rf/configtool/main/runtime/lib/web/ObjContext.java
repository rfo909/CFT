package rf.configtool.main.runtime.lib.web;

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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjClosure;
import rf.configtool.main.runtime.lib.ObjDict;

/**
 * A context is a URL path with closures for GET and POST.
 * 
 * In this primitive web server, different content types are
 * tied in with different context objects. 
 */

public class ObjContext extends Obj {
    
    private ObjServer server;
    private String path;
    private String contentType;
    
    private ObjClosure closureGET;
    private ObjClosure closurePOST;
    
    public ObjContext(ObjServer server, String path) {
        this(server, path, "text/html");
    }
    
    public ObjContext(ObjServer server, String path, String contentType) {
        
        this.contentType=contentType;
        this.server=server;
        this.path=path;
        
        server.bind(path,this);

        this.add(new FunctionContext());
        this.add(new FunctionGET());
        this.add(new FunctionPOST());
    }
    
    public ObjClosure getClosureGET() {
        return closureGET;
    }
    
    
    public ObjClosure getClosurePOST() {
        return closurePOST;
    }
    
    
    public String getContentType() {
        return contentType;
    }
    
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "WebServerContext";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "WebServerContext";
    }
    
    private ObjServer theServer() {
        return server;
    }
    
    private ObjContext theContext () {
        return this;
    }
    
    class FunctionContext extends Function {
        public String getName() {
            return "Context";
        }
        public String getShortDesc() {
            return "Context(subPath, contentType?) - returns sub-context object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1 && params.size() != 2) throw new Exception("Expected path, contentType(optional) string parameters");
            String subPath=getString("subPath", params, 0);
            String contentType="text/html";
            if (params.size()==2) contentType=getString("contentType", params, 1);
            return new ValueObj(new ObjContext(server, (path+subPath).replace("//", "/"), contentType));
        }
    }
    
    
    class FunctionGET extends Function {
        public String getName() {
            return "GET";
        }
        public String getShortDesc() {
            return "GET(LambdaOrClosure) - set GET lambda, return self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected lambda or closure parameter");
            
            Obj obj=params.get(0);
            if (obj instanceof ValueBlock) {
                ValueBlock lambda=(ValueBlock) obj;
                ObjClosure closure=new ObjClosure(new ObjDict(), lambda);
                closureGET=closure;
            } else if (obj instanceof ValueObj) {
                Obj x = ((ValueObj) obj).getVal(); 
                if (!(x instanceof ObjClosure)) throw new Exception("Expected lambda or closure parameter");
                closureGET=(ObjClosure) x;
            } else {
                throw new Exception("Expected lambda or closure parameter");
            }
            return new ValueObj(theContext());
        }
    }
    
    
    class FunctionPOST extends Function {
        public String getName() {
            return "POST";
        }
        public String getShortDesc() {
            return "POST(LambdaOrClosure) - set POST lambda, return self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected lambda or closure parameter");
            
            Obj obj=params.get(0);
            if (obj instanceof ValueBlock) {
                ValueBlock lambda=(ValueBlock) obj;
                ObjClosure closure=new ObjClosure(new ObjDict(), lambda);
                closurePOST=closure;
            } else if (obj instanceof ValueObj) {
                Obj x = ((ValueObj) obj).getVal(); 
                if (!(x instanceof ObjClosure)) throw new Exception("Expected lambda or closure parameter");
                closurePOST=(ObjClosure) x;
            } else {
                throw new Exception("Expected lambda or closure parameter");
            }
            return new ValueObj(theContext());
        }
    }
    
    

}
