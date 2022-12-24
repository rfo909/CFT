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

package rf.configtool.main.runtime.lib.text;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueString;

public class ObjLexerToken extends Obj {
    
    private String sourceLocation;
    private int tokenType;
    private String str;
    
    public ObjLexerToken(String sourceLocation, int tokenType, String str) {
        this.sourceLocation=sourceLocation;
        this.tokenType=tokenType;
        this.str=str;
        
        this.add(new FunctionSourceLocation());
        this.add(new FunctionTokenType());
        this.add(new FunctionStr());
    }
    
    public String getSourceLocation() {
        return sourceLocation;
    }
    
    public int getTokenType() {
        return tokenType;
    }
    
    public String getStr() {
        return str;
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Lexer.Token";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Lexer.Token";
    }
    
    private Obj theObj () {
        return this;
    }
    

    
    class FunctionSourceLocation extends Function {
        public String getName() {
            return "sourceLocation";
        }
        public String getShortDesc() {
            return "sourceLocation() - return source location string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(sourceLocation);
        }
    }
    

    class FunctionTokenType extends Function {
        public String getName() {
            return "tokenType";
        }
        public String getShortDesc() {
            return "tokenType() - return token type (int)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(tokenType);
        }
    }
    

    class FunctionStr extends Function {
        public String getName() {
            return "str";
        }
        public String getShortDesc() {
            return "str() - return token string representation string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(str);
        }
    }
    

}
