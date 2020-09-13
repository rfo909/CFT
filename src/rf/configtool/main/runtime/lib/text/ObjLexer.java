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
import rf.configtool.parser.CharTable;

import java.awt.Color;

public class ObjLexer extends Obj {
    
    public ObjLexer() {
    	this.add(new FunctionNode());
    	this.add(new FunctionProcessLine());
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
    
    
   
    private List<ObjLexerToken> tokenList=new ArrayList<ObjLexerToken>();
    
    
    
    class FunctionProcessLine extends Function {
        public String getName() {
            return "processLine";
        }
        public String getShortDesc() {
            return "processLine(rootNode,line,eolTokenType?) - processes line, adds to internal token list - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() < 2 || params.size() > 3) throw new Exception("Expected parameters rootNode, line, eolTokenType?");
            
            Obj obj=getObj("rootNode",params,0);
            if (!(obj instanceof ObjLexerNode)) throw new Exception("Expected parameters rootNode and line");
             
            CharTable charTable=((ObjLexerNode) obj).getCharTable();
            
            // if the line was read from a file, then it is a ValueObjFileLine (which subclasses ValueString)
            String filePlusLineNo="";
            if (params.get(1) instanceof ValueObjFileLine) {
            	ValueObjFileLine x = (ValueObjFileLine) params.get(1);
            	filePlusLineNo=x.getFile().getPath() + " line=" + x.getLineNo() + " ";
            }
            // get line string
        	String line = getString("line",params, 1);
        	CharSource cs=new CharSource(line);
        	
        	// get optional token type to add at end of line, useful when parsing files
        	Integer eolTokType=null;
        	if (params.size()==3) {
        		eolTokType=(int) getInt("eolTokenType", params, 2);
        	}
        	
        	while (!cs.eol()) {
        		String sourceLocation=filePlusLineNo+"pos=" + (cs.getPos()+1);
        		
        		int startPos=cs.getPos();
	        	Integer tokenType = charTable.parse(cs);
	        	if (tokenType == null) {
	        		if (!cs.eol()) throw new Exception(sourceLocation + ": lexer failed, current char = '" + cs.getChar() + "'");
	        	}
	        	
	        	if (tokenType < 0) continue; // ignoring these: whitespace and comments
	        	
	        	int nextPos=cs.getPos();
	        	ObjLexerToken token=new ObjLexerToken(sourceLocation, tokenType, line.substring(startPos, nextPos));
	        	tokenList.add(token);
        	}
        	
        	if (eolTokType != null) {
        		String sourceLocation=filePlusLineNo + "(end-of-line)";
        		tokenList.add(new ObjLexerToken(sourceLocation, eolTokType, "(end-of-line)"));
        	}
        	
        	return new ValueObj(theObj());
        }
        	
    }
    
   
    class FunctionGetTokens extends Function {
        public String getName() {
            return "getTokens";
        }
        public String getShortDesc() {
            return "getTokens() - get list of tokens identified via calls to processLine";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	List<Value> valueList=new ArrayList<Value>();
        	for (ObjLexerToken t:tokenList) {
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
            return "getTokenStream() - get list of tokens identified via processLine as TokenStream object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	return new ValueObj(new ObjLexerTokenStream(tokenList));
        }
    }
    

}
