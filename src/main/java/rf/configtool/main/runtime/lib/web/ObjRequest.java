package rf.configtool.main.runtime.lib.web;

import java.util.List;
import java.util.Map;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;


public class ObjRequest extends Obj {
    
	private Map<String,String> headers;
	private String method;
	private String url;
	private byte[] body;

	
	public ObjRequest(Map<String,String> headers, String method, String url, byte[] body) {
		this.headers = headers;
		this.method = method;
		this.url = url;
		this.body=body;

		this.add(new FunctionHeaders());
		this.add(new FunctionMethod());
		this.add(new FunctionUrl());
		this.add(new FunctionBody());
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
    
    
    
    
    
    
    private Value createStringDict (Map<String, String> data) {
		ObjDict dict = new ObjDict();
		for (String name:data.keySet()) {
			Value v=new ValueString(data.get(name));
			dict.set(name, v);
		}
		return new ValueObj(dict);

    }
    

}
