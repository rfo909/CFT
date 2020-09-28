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

package rf.configtool.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.parser.TokenStream;

/**
 * Return directory object for current directory
 */
public class ExprTryUnsafe extends LexicalElement {
	
	private Expr expr;

    public ExprTryUnsafe (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("tryUnsafe","expected 'tryUnsafe'");
        ts.matchStr("(", "expected '(' following tryUnsafe");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing tryUnsafe() expression");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
    	Value result=null;
    	ValueList stackTrace=null;
    	Value msg=null;
    	try {
    		result = expr.resolve(ctx);
    	} catch (Exception ex) {
    		stackTrace=getStackTrace(ex);
    		msg=new ValueString(ex.getMessage());
    	}
    	HashMap<String,Value> data=new HashMap<String,Value>();
    	data.put("result", result);
    	data.put("ok", new ValueBoolean(result != null));
    	data.put("stack", stackTrace);
    	data.put("msg", msg==null ? new ValueNull() : msg);
    	return new ValueObj(new ObjDict(data));
    }
    
    private ValueList getStackTrace(Exception ex) {
    	List<Value> data=new ArrayList<Value>();
    	if (ex instanceof SourceException) {
    		Exception x=((SourceException) ex).getOriginalException();
    		if (x != null) ex=x;
    	}
    	StackTraceElement[] st = ex.getStackTrace();
    	for (StackTraceElement e:st) {
    		data.add(new ValueString(e.toString()));
    	}
    	return new ValueList(data);
    }
}
