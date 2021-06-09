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
import rf.configtool.main.SoftErrorException;
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
        this.add(new FunctionEOF());
        this.add(new FunctionPeek());
        this.add(new FunctionMatch());
        this.add(new FunctionPeekType());
        this.add(new FunctionNext());
    }
    
    private ObjLexerToken curr() throws Exception {
        if (pos >= tokens.size()) throw new Exception("At EOF");
        return tokens.get(pos);
    }
    
    private Obj self() {
        return this;
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
    
    
    class FunctionEOF extends Function {
        public String getName() {
            return "EOF";
        }
        public String getShortDesc() {
            return "EOF() - true when end of data";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBoolean(pos >= tokens.size());
        }
    }
    
    class FunctionPeek extends Function {
        public String getName() {
            return "peek";
        }
        public String getShortDesc() {
            return "peek() - string represenation of next token";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(curr().getStr());
        }
    }
    
    class FunctionMatch extends Function {
        public String getName() {
            return "match";
        }
        public String getShortDesc() {
            return "match(str, errMsg?) - if match, advance curr pos and return true, otherwise, if errMsg, throw soft error, otherwise just return false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String str;
            String errMsg;
            if (params.size() == 1 || params.size() == 2) {
                  str=getString("str", params, 0);
                  if (params.size()==2) {
                    errMsg=getString("errMsg", params, 1);
                  } else {
                    errMsg=null;
                  }
            } else {
                throw new Exception("Expected parameters str, errMsg?");
            }
            
            if (curr().getStr().equals(str)) {
                pos++;
                return new ValueBoolean(true);
            } else {
                if (errMsg != null) throw new SoftErrorException(curr().getSourceLocation() + " " + errMsg);
                return new ValueBoolean(false);
            }
        }
    }
    
    class FunctionPeekType extends Function {
        public String getName() {
            return "peekType";
        }
        public String getShortDesc() {
            return "peekType() - type (int) of next token";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(curr().getTokenType());
        }
    }
        
    class FunctionNext extends Function {
        public String getName() {
            return "next";
        }
        public String getShortDesc() {
            return "next() - move to next token";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            pos++;
            return new ValueObj(self());
        }
    }

    
    
    
}
