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
import rf.configtool.parser.CharSource;
import rf.configtool.parser.CharTable;
import rf.configtool.parser.Parser;

import java.awt.Color;

/**
 * Result object from functions inside Lexer,
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
		this.add(new FunctionMatch());
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
			return "setIsToken(tokenType) - tokenType is an int - returns self";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 1)
				throw new Exception("Expected tokenType (int) parameter");
			int tokenType = (int) getInt("tokenType", params, 0);
			charTable.setTokenType(tokenType);
			return new ValueObj(self());
		}
	}

	class FunctionMatch extends Function {
		public String getName() {
			return "match";
		}

		public String getShortDesc() {
			return "match(Str) - returns number of characters matched";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 1)
				throw new Exception("Expected Str parameter");
			String str = getString("Str", params, 0);
			CharSource cs = new CharSource(str);

			Integer tokenType = charTable.parse(cs);
			if (tokenType == null) {
				return new ValueInt(0);
			} else {
				return new ValueInt(cs.getPos());
			}
		}
	}

}
