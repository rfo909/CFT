/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.CharSource;
import rf.configtool.lexer.CharSourcePos;
import rf.configtool.lexer.CharTable;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ValueObjFileLine;

/**
 * CFT object for doing lexical analysis in script code.
 *
 */
public class ObjLexer extends Obj {
    
    public ObjLexer() {
        this.add(new FunctionNode());
        this.add(new FunctionAddLine());
        this.add(new FunctionGetTokens());
        this.add(new FunctionGetTokenStream());
        this.add(new Function_Example());
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
                ValueObjFileLine x = (ValueObjFileLine) params.get(0);
                file=x.getFile().getPath();
            } else {
                file="(nofile)";
            }
            // get line (as string) string
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
    
    
    class Function_Example extends Function {
        public String getName() {
            return "_Example";
        }
        public String getShortDesc() {
            return "_Example() - display example";
        }
        private String[] data= {
                "",
                "Example (based on JSON script)",
                "------------------------------",
                "",
                "# Build root lexer Node for all known tokens in JSON",
                "# --",
                "    Lib.Text.Lexer.Node => root",
                "    ",
                "    root.sub(\"{}:,[]()\").setIsToken(1)  # specials",
                "    root.sub(\" ^n^r^t\".unEsc).setIsToken(-1) # whitespace",
                "",
				"    comment=root.sub(\"#\")	# comments",
				"    comment.setDefault(comment)",
				"    comment.sub(\"^n\".unEsc).setIsToken(-1)",
				"",
                "    digits = \"0123456789\"",
                "    root.sub(digits+\"-\").setIsToken(2) => integer",
                "    integer.sub(digits,integer) # loop back",
                "    integer.sub(\".\").sub(digits).setIsToken(3) => float ",
                "    float.sub(digits,float) # loop back",
                "    ",
                "    identFirstChars = \"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_\"",
                "    identInnerChars = identFirstChars + digits",
                "    ",
                "    root.sub(identFirstChars).setIsToken(4) => ident",
                "    ident.sub(identInnerChars, ident) # loop back",
                "    ",
                "    List('\"',\"'\")->c ",
                "        root.sub(c) => insideString",
                "        insideString.setDefault(insideString)",
                "        insideString.sub(\"\\\").setDefault(insideString)",
                "        insideString.sub(c).setIsToken(5)",
                "    |",
                "    ",
                "    # return value is the root node",
                "    root",
                "/RootNode",
                "",
                "# Takes json string as parameter",
                "# --",
                "    P(1)=>json",
                "",
                "    Lib.Text.Lexer => lexer",
                "    json->line ",
                "        lexer.addLine(line) ",
                "    |",
                "",
                "    RootNode => root",
                "    ts=lexer.getTokenStream(root)",
                "",
                "    MatchValue(ts)",
                "/Parse",
                "",
                "For details on how to build a recursive-descent parser,",
                "see the JSON parser script.",
                "",
                ":load JSON"
        };
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (String line:data) {
                ctx.getObjGlobal().addSystemMessage(line);
            }
            return new ValueBoolean(true);
        }
    }


}
