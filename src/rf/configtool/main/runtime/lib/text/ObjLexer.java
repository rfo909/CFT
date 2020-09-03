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

public class ObjLexer extends Obj {
    
    public ObjLexer() {
    	this.add(new FunctionEmpty());
    	this.add(new FunctionIdentifier());
    	this.add(new FunctionLPAR());
    	this.add(new FunctionRPAR());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Lexer";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Lexer";
    }
    
    private Obj theObj () {
        return this;
    }
    

    
    class FunctionEmpty extends Function {
        public String getName() {
            return "Empty";
        }
        public String getShortDesc() {
            return "Empty(firstChars?) - create Empty node, possibly identifying firstChars list";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	String firstChars=ObjLexerNode.NO_CHARS;
            if (params.size() == 1) {
            	firstChars=getString("firstChars",params,0);
            }
            return new ValueObj(new ObjLexerNode(firstChars));
        }
    }
    
    
    class FunctionIdentifier extends Function {
        public String getName() {
            return "Identifier";
        }
        public String getShortDesc() {
            return "Identifier() - create Identifier node";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjLexerNodeIdentifier());
        }
    }
    

    class FunctionLPAR extends Function {
        public String getName() {
            return "LPAR";
        }
        public String getShortDesc() {
            return "LPAR() - create LPAR node - matches '('";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjLexerNodeSingleChar('('));
        }
    }
    

    class FunctionRPAR extends Function {
        public String getName() {
            return "RPAR";
        }
        public String getShortDesc() {
            return "RPAR() - create RPAR node - matches ')'";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjLexerNodeSingleChar(')'));
        }
    }
    

    

}
