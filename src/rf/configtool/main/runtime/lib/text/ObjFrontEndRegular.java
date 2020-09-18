package rf.configtool.main.runtime.lib.text;

import java.util.HashMap;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;

public class ObjFrontEndRegular extends Obj implements FrontEnd {

	private ObjLexerTokenStream tokenStream;
	private HashMap<String, Integer> tokenTypes = new HashMap<String, Integer>();

	public ObjFrontEndRegular(ObjLexerTokenStream tokenStream) {
		this.tokenStream=tokenStream;
		this.add(new FunctionToken());
//        this.add(new FunctionLexer());
//        this.add(new FunctionParserDynamic());
	}

	@Override
	public boolean eq(Obj x) {
		return x == this;
	}

	@Override
	public String getTypeName() {
		return "Parser.FrontEndRegular";
	}

	@Override
	public ColList getContentDescription() {
		return ColList.list().regular(getDesc());
	}

	private String getDesc() {
		return getTypeName();
	}

	private Obj theObj() {
		return this;
	}

	// ---------------------------------------
	// FrontEnd interface
	// ---------------------------------------

	public FrontEndState getInputPos() {
		return null;
	}

	public void setInputPos(FrontEndState mark) {
		// todo
	}

	/**
	 * Match token and return Value, or Java null if failing
	 */
	public Value matchToken(String tokenSelectionString) throws Exception {
		return null;
	}
	
	public String getSourceLocation() {
		return "";
	}


	// ---------------------------------------

	class FunctionToken extends Function {
		public String getName() {
			return "Token";
		}

		public String getShortDesc() {
			return "Token(name,tokenType) - add token definition - returns self";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 2)
				throw new Exception("Expected parameters name, tokenType (int)");
			String name = getString("name", params, 0);
			Integer tokenType = (int) getInt("tokenType", params, 1);

			tokenTypes.put(name, tokenType);

			return new ValueObj(theObj());
		}
	}

}
