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
import rf.configtool.main.SoftException;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.parser.TokenStream;

/**
 * Try-catch for soft errors (created by error()) 
 */
public class ExprTryCatchSoft extends LexicalElement {
	
	private Expr expr;

    public ExprTryCatchSoft (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("tryCatchSoft","expected 'tryCatchSoft'");
        ts.matchStr("(", "expected '(' following tryCatchSoft");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing tryCatchSoft() expression");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
    	SoftException softEx=null;
    	
    	Value result=null;

    	try {
    		result = expr.resolve(ctx);
    	} catch (Exception ex) {
    		// examine if SoftException
    		//
    		if (ex instanceof SoftException) {
    			softEx=(SoftException) ex;
    		} else if (ex instanceof SourceException) {
    			Exception inner=((SourceException) ex).getOriginalException();
    			if (inner != null && (inner instanceof SoftException)) {
    				softEx=(SoftException) inner;
    			}
    		}
    		if (softEx==null) throw ex;  
    	}
    	
    	HashMap<String,Value> data=new HashMap<String,Value>();
    	if (result != null) data.put("result", result);
    	data.put("ok", new ValueBoolean(result != null));
    	if (softEx != null) data.put("msg", new ValueString(softEx.getMessage()));
  
    	return new ValueObj(new ObjDict(data));
    }
    
}
