/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

package rf.configtool.main.runtime.lib.web;

import java.util.List;
import java.util.Map;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;


public class ObjWebRequest extends Obj {
    
    private Map<String,String> headers;
    private String method;
    private String url;
    private Map<String,String> urlParams;
    private byte[] body;
    private Map<String,String> bodyParams;

    public ObjWebRequest(Map<String,String> headers, String method, String url, Map<String,String> urlParams, byte[] body, Map<String,String> bodyParams) {
        this.headers = headers;
        this.method = method;
        this.url = url;
        this.urlParams=urlParams;
        this.body=body;
        this.bodyParams=bodyParams;

        this.add(new FunctionHeaders());
        this.add(new FunctionMethod());
        this.add(new FunctionUrl());
        this.add(new FunctionURLParams());
        this.add(new FunctionBody());
        this.add(new FunctionBodyParams());
    }

    public Map<String,String> getHeaders() {
        return headers;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getUrl() {
        return url;
    }
    
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "WebRequest";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "WebRequest";
    }
    
    class FunctionHeaders extends Function {
        public String getName() {
            return "headers";
        }
        public String getShortDesc() {
            return "headers() - returns Dict";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return createStringDict(headers);
        }
    }
    
    class FunctionMethod extends Function {
        public String getName() {
            return "method";
        }
        public String getShortDesc() {
            return "method() - returns String";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(method);
        }
    }
    
    
 
    class FunctionUrl extends Function {
        public String getName() {
            return "url";
        }
        public String getShortDesc() {
            return "url() - returns String";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(url);
        }
    }
    
    class FunctionURLParams extends Function {
        public String getName() {
            return "urlParams";
        }
        public String getShortDesc() {
            return "urlParams() - returns Dict (may be empty)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return createStringDict(urlParams);
        }
    }
    

    
    class FunctionBody extends Function {
        public String getName() {
            return "body";
        }
        public String getShortDesc() {
            return "body() - returns binary or null";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            if (body==null) return new ValueNull();
            return new ValueBinary(body);
        }
    }

    class FunctionBodyParams extends Function {
        public String getName() {
            return "bodyParams";
        }
        public String getShortDesc() {
            return "bodyParams() - returns Dict of encoded form data from body (may be empty)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return createStringDict(bodyParams);
        }
    }
    


    
    
    
    
    
    private Value createStringDict (Map<String, String> data) {
        ObjDict dict = new ObjDict();
        if (data==null) return new ValueObj(dict);
        
        for (String name:data.keySet()) {
            Value v=new ValueString(data.get(name));
            dict.set(name, v);
        }
        return new ValueObj(dict);

    }
    

}
