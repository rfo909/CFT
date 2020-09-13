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

package rf.configtool.main.runtime.lib.text;

import java.io.*;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

public class ObjLexerTokenStream extends Obj {
    
	private List<ObjLexerToken> tokens;
	private int pos=0;
	
    public ObjLexerTokenStream (List<ObjLexerToken> tokens) {
    	this.tokens=tokens;
    	
    	this.add(new FunctionSourceLocation());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "LexerTokenStream";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "LexerTokenStream";
    }
    
    private Obj theObj () {
        return this;
    }
    

    
    class FunctionSourceLocation extends Function {
        public String getName() {
            return "sourceLocation";
        }
        public String getShortDesc() {
            return "sourceLocation() - return source location for next token";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no parameters");
        	if (pos >= tokens.size()) {
        		return new ValueString("<EOF>");
        	}
        	return new ValueString(tokens.get(pos).getSourceLocation());
        }
    }
    
    
    
}
