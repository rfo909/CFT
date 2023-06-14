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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.CharTable;
import rf.configtool.lexer.StopRule;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDict;

/**
 * Result object from functions inside Lexer. Basically a wrapper around the CharTable object.
 */
public class ObjLexerNode extends Obj {

    public static final String NO_CHARS = "";

    private String firstChars;
    private CharTable charTable;
    
    private ObjLexerNode() {
        this.add(new FunctionSub());
        this.add(new FunctionAddToken());
        this.add(new FunctionSetDefault());
        this.add(new FunctionSetIsToken());
        this.add(new FunctionAddTokenComplex());
        this.add(new FunctionStop());
        this.add(new FunctionDump());
    }

    public ObjLexerNode(String firstChars) {
        this();
        this.firstChars = firstChars;
        this.charTable = new CharTable();
    }

    private ObjLexerNode(CharTable charTable) {
        this();
        this.firstChars = null;
        this.charTable = charTable;
    }

    // ------

    public CharTable getCharTable() {
        return charTable;
    }

    public String getFirstChars() {
        return firstChars;
    }

    protected void setMapping(String chars, CharTable ct) {
        charTable.setMapping(chars, ct);
    }

    public void setIsToken() {
        charTable.setTokenType(1);
    }

    public void setDefaultMapping(ObjLexerNode node) {
        charTable.setDefaultMapping(node.getCharTable());
    }
    
    public Obj theObj() {
        return this;
    }

    private ObjLexerNode addToken(String token) {
        CharTable curr = charTable;

        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);

            CharTable sub = curr.getMapping(c);
            if (sub == null) {
                sub = new CharTable();
                curr.setMapping(c, sub);
            }
            curr = sub;
        }
        return new ObjLexerNode(curr);
    }
    
    protected List<Value> show () {
    	List<String> lines=new ArrayList<String>();
    	charTable.show("", lines);
    	
    	List<Value> result=new ArrayList<Value>();
    	for (String x:lines) result.add(new ValueString(x));
    	return result;
    }

    /**
     * The charMap maps single characters to multiple characters, so that
     * the token may be defined as "iiii-ii-ii", mapping "i" to "0123456789" in
     * order to match dates. Note: can not be as "nice" as addToken(), which
     * checks if there exists sub-nodes for mapping. This one runs blindly and
     * overwrites any other "shared" mappings.
     */
    private ObjLexerNode addTokenComplex(String token, ObjDict charMap) {
        CharTable curr = charTable;

        for (int i = 0; i < token.length(); i++) {
            char x = token.charAt(i);

            String chars;
            // check if x maps to set of characters
            Value v=charMap.getValue(""+x);
            if (v != null) {
                chars=v.getValAsString();
            } else {
                chars=""+x;
            }
            
            CharTable sub = new CharTable();
            curr.setMapping(chars, sub);

            curr = sub;
        }
        return new ObjLexerNode(curr);
    }

    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    @Override
    public String getTypeName() {
        return "Lexer.Node";
    }

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "Lexer.Node";
    }

    private Obj self() {
        return this;
    }

    class FunctionSub extends Function {
        public String getName() {
            return "sub";
        }

        public String getShortDesc() {
            return "sub(chars, targetNode) or sub(chars) or sub(targetNode) - add mapping, returns target Node";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            String chars = null;
            ObjLexerNode node = null;

            if (params.size() == 1) {
                if (params.get(0) instanceof ValueString) {
                    chars = getString("chars", params, 0);
                } else {
                    node = (ObjLexerNode) getObj("targetNode", params, 0);
                }
            } else if (params.size() == 2) {
                chars = getString("chars", params, 0);
                node = (ObjLexerNode) getObj("targetNode", params, 1);
            } else {
                throw new Exception("Expected params chars, targetNode?");
            }
            if (chars == null) {
                chars = node.getFirstChars();
            } else if (node == null) {
                node = new ObjLexerNode(NO_CHARS);
            }
            setMapping(chars, node.getCharTable());

            return new ValueObj(node);
        }
    }

    class FunctionAddToken extends Function {
        public String getName() {
            return "addToken";
        }

        public String getShortDesc() {
            return "addToken(token) - create mappings for token string, returns resulting Node";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1)
                throw new Exception("Expected token parameter (string)");
            String token = getString("token", params, 0);
            return new ValueObj(addToken(token));
        }
    }

    class FunctionSetDefault extends Function {
        public String getName() {
            return "setDefault";
        }

        public String getShortDesc() {
            return "setDefault(targetNode?) - map all non-specified characters to node, returns target node";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            ObjLexerNode node = null;
            if (params.size() == 1) {
                node = (ObjLexerNode) getObj("node", params, 0);
            } else if (params.size() == 0) {
                node = new ObjLexerNode(ObjLexerNode.NO_CHARS);
            } else {
                throw new Exception("Expected optional targetNode parameter");
            }
            charTable.setDefaultMapping(node.getCharTable());
            return new ValueObj(node);
        }
    }

    class FunctionSetIsToken extends Function {
        public String getName() {
            return "setIsToken";
        }

        public String getShortDesc() {
            return "setIsToken(tokenType?) - tokenType is an int, which defaults to 0 - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() > 1)
                throw new Exception("Expected optional tokenType (int) parameter");
            int tokenType;
            if (params.size()==1) {
                tokenType = (int) getInt("tokenType", params, 0);
            } else {
                tokenType=0;
            }
            charTable.setTokenType(tokenType);
            return new ValueObj(self());
        }
    }
    
    class FunctionAddTokenComplex extends Function {
        public String getName() {
            return "addTokenComplex";
        }

        public String getShortDesc() {
            return "addTokenComplex(token, charMapDict) - create mappings for complex string, returns resulting Node";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2)
                throw new Exception("Expected parameters token (string) and charMapDict");
            String token = getString("token", params, 0);
            Obj obj = getObj("charMapDict", params, 1);
            if (!(obj instanceof ObjDict)) {
                throw new Exception("Expected parameters token (string) and charMapDict");
            }
            ObjDict charMap=(ObjDict) obj;
            return new ValueObj(addTokenComplex(token, charMap));
        }
    }

    class FunctionStop extends Function {
        public String getName() {
            return "STOP";
        }

        public String getShortDesc() {
            return "STOP() - returns STOP Node";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	ObjLexerNode node = new ObjLexerNode(StopRule.STOP);
            return new ValueObj(node);
        }
    }

    
    class FunctionDump extends Function {
        public String getName() {
            return "dump";
        }

        public String getShortDesc() {
            return "dump() - returns list of strings, showing tree under node";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	List<Value> lines=show();
        	return new ValueList(lines);
        }
    }


}
