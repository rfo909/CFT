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
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.parser.CharSource;
import rf.configtool.parser.CharSourcePos;
import rf.configtool.parser.CharTable;
import rf.configtool.parser.SourceLocation;

import java.awt.Color;

public class ObjLexer extends Obj {
    
    public ObjLexer() {
    	this.add(new FunctionNode());
    	this.add(new FunctionAddLine());
    	this.add(new FunctionGetTokens());
    	this.add(new FunctionGetTokenStream());
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
    

    
    class FunctionNode extends Function {
        public String getName() {
            return "Node";
        }
        public String getShortDesc() {
            return "Node(firstChars?) - create empty node, possibly identifying firstChars list";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	String firstChars=ObjLexerNode.NO_CHARS;
            if (params.size() == 1) {
            	firstChars=getString("firstChars",params,0);
            }
            return new ValueObj(new ObjLexerNode(firstChars));
        }
    }
    
    
   
    private CharSource cs=new CharSource();
    private int lineNo=0;
    
    class FunctionAddLine extends Function {
        public String getName() {
            return "addLine";
        }
        public String getShortDesc() {
            return "addLine(line) - processes line, adds to internal token list - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter: line");
            
            lineNo++;
            
            // if the line was read from a file, then it is a ValueObjFileLine (which subclasses ValueString)
            String file;
            
            if (params.get(0) instanceof ValueObjFileLine) {
            	ValueObjFileLine x = (ValueObjFileLine) params.get(1);
            	file=x.getFile().getPath();
            } else {
            	file="(nofile)";
            }
            // get line string
        	String line = getString("line",params, 0);
        	cs.addLine(line, new SourceLocation(file,lineNo));
        	
        	
        	return new ValueObj(theObj());
        }
        	
    }

     

    
    private List<ObjLexerToken> identifyTokens(CharTable charTable) throws Exception {
    	List<ObjLexerToken> tokenList=new ArrayList<ObjLexerToken>();
    	
    	cs.reset();

    	while (!cs.eof()) {
    		CharSourcePos startPos=cs.getPos();
    		SourceLocation loc=cs.getSourceLocation(startPos);
    		
        	Integer tokenType = charTable.parse(cs);
        	if (tokenType == null) {
        		if (!cs.eof()) throw new Exception(loc + ": lexer failed, current char = '" + cs.getChar() + "'");
        	}
        	
        	if (tokenType < 0) continue; // ignoring these: whitespace and comments
        	
        	ObjLexerToken token=new ObjLexerToken(loc.toString(), tokenType, cs.getChars(startPos));
        	tokenList.add(token);
    	}
    	
    	return tokenList;
    }
   
    
    class FunctionGetTokens extends Function {
        public String getName() {
            return "getTokens";
        }
        public String getShortDesc() {
            return "getTokens(rootNode) - get list of tokens";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected rootNode");

        	cs.reset();

        	Obj obj=getObj("rootNode",params,0);
            if (!(obj instanceof ObjLexerNode)) throw new Exception("Expected parameters rootNode and line");
        	CharTable charTable=((ObjLexerNode) obj).getCharTable();
    
        	List<Value> valueList=new ArrayList<Value>();
        	for (ObjLexerToken t:identifyTokens(charTable)) {
        		valueList.add(new ValueObj(t));
        	}
        	return new ValueList(valueList);
        }
    }
    

    class FunctionGetTokenStream extends Function {
        public String getName() {
            return "getTokenStream";
        }
        public String getShortDesc() {
            return "getTokenStream(rootNode) - get TokenStream object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected rootNode");

        	Obj obj=getObj("rootNode",params,0);
            if (!(obj instanceof ObjLexerNode)) throw new Exception("Expected parameters rootNode and line");
        	CharTable charTable=((ObjLexerNode) obj).getCharTable();
    
        	return new ValueObj(new ObjLexerTokenStream(identifyTokens(charTable)));
        }
    }

}
