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
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.parser.CharSource;
import rf.configtool.parser.CharTable;

import java.awt.Color;

/**
 * 
 * This is an implementation of a recursive-descent parser. It contains named productions that 
 * contain tokens (in the form of CharTable) and non-terminals (other productions) identified by strings. 
 * 
 * Contrary to the parser of CFT, which has a discrete lexer pass before parsing (by means of
 * Java classes invoking each other), this parser must be more flexible, as particularly 
 * log lines processing may require custom token matchers at individual stages, moving from
 * left to right through the log line. Such token patterns may well be mutually exclusive, which
 * means they can not be gathered under a common CharSet (Node), which is particularly obvious
 * for special "match-rest-of-line" tokens. 
 * 
 * This means there is no way to tokenize the entire input first. The lexer and parser need 
 * to work together in a single pass.
 *
 * On the other hand, it should also be able to work on a token stream, which will usually mean
 * a unified Node tree, but that's irrelevant. 
 * 
 * The grammar contains my own extension over EBNF format. It does not allow multiple tokens to be
 * matched directly, only indirectly via productions. My addition to EBNF is support for calls to separator
 * productions as part of multiple-match with '*' and '+'. Also note that naming of matched results,
 * will use production names. Multiple references to same sub-production within a production may 
 * cause naming conflict when storing results.
 * 
 * There is also the special symbol for "Point of no return", or PONR, which should be interspersed
 * in the grammar at points where a certain symbol or completely matched production makes
 * backtracking an error. The CFT grammar has no explicit such symbol, as it uses lookahead as its 
 * points of no return.
 * 
 * In a non-strict more of a free form parser, and without PONR, errors in the input would
 * result in the parser backtracking all the way to the start, in effect just informing us 
 * that input "does not compute". One way of producing more sensible error messages, is by 
 * recording the max position that has been accessed in the CharSet, but even this can give 
 * false readings, as different alternatives in a complex grammar can even have reached 
 * end-of-line (or end-of-data) as part of processing alternatives which turned out to not parse
 * the input. 
 * 
 * Reaching a PONR is a way to establish that we can not backtrack past it, and it also signifies that
 * previous advancements in the CharSource are irrelevant, and may reset the "max" position to
 * the current position.
 *
 * This parser named "dynamic", due to its free-form, and is line-based.
 *
 */
public class ObjParser extends Obj {
	
	public static final String PONR = ">>>";
   
    public ObjParser() {
    	this.add(new FunctionReadme());
    	this.add(new FunctionProd());
    	this.add(new FunctionUseFrontEndAdHoc());
    	this.add(new FunctionUseFrontEndRegular());
    //	this.add(new FunctionProcessLine());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Parser";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Parser";
    }
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionReadme extends Function {
        public String getName() {
            return "readme";
        }
        public String getShortDesc() {
            return "readme() - returns info on the Parser object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no parameters");
        	String[] lines= {
        		"The Parser object",
        		"-----------------",
        		"The Parser is powered by a grammar. It is created from a set of named",
        		"productions, which are based on EBNF (Extended Backus-Naur Form), and then",
        		"extended a bit more.",
        		"",
        		"A production has a name, and a sequence of elements to match. These take",
        		"two forms: either a non-terminal, which refers to other productions by name,",
        		"which is simply a String, or a terminal, which refers to an object that",
        		"implements the TokenMatcher Java Interface.",
        		"",
        		"That currently means a Node object, as created via the Lexer object.",
        		"",
        		"Example:",
        		"--------",
        		"Lib.Text.Lexer.Node.addToken('{').setIsToken(5) =leftCurl",
        		"Lib.Text.Lexer.Node.addToken('}').setIsToken(5) =rightCurl",
        		"Lib.Text.Lexer.Node.addToken(',').setIsToken(5) =comma",
        		"Lib.Text.Lexer.Node.addToken(':').setIsToken(5) =colon",
        		"",
        		"Lib.Text.Parser =parser",
        		"parser.prod('object', leftCurl, 'field*sepComma', rightCurl)",
        		"parser.prod('sepComma', comma)",
        		"parser.prod('field', 'fieldName', colon, 'value')",
        		"   ...",
        		"",
        		"The references to other productions may be expressed as follows:",
        		"  - 'name'    - matches production once",
        		"  - 'name?'   - optional match",
        		"  - 'name*'   - matches zero or more times",
        		"  - 'name+'   - one or more times",
        		"  - 'name*sep - zero or more time, with second production 'sep' between",
        		"  - 'name+sep - one or more times with separator production",
        		"",
        		//"Naming of matched elements are as follows:",
        		//"",
        	};
        	List<Value> list=new ArrayList<Value>();
        	for (String s:lines) {
        		list.add(new ValueString(s));
        	}
        	return new ValueList(list);
        }
    }
    
 
    private final ProductionsAll productions = new ProductionsAll();
    
    class FunctionProd extends Function {
        public String getName() {
            return "prod";
        }
        public String getShortDesc() {
            return "prod(name, ...) - add grammar production - see readme() - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() < 2) throw new Exception("Expected parameters name, ...");
        	
        	String name=getString("name", params, 0);
        	List<Object> elements=new ArrayList<Object>();
        	
        	for (int i=1; i<params.size(); i++) {
        		Value v=params.get(i);
        		if (v instanceof ValueString) {
        			// add non-terminal or control symbol (PONR)
        			elements.add( ((ValueString) v).getVal() );
        			continue;
        		} else if (v instanceof ValueObj) {
        			Obj obj=((ValueObj) v).getVal();
        			if (obj instanceof ObjLexerNode) {
        				// terminal : token - save CharTable
        				elements.add( ((ObjLexerNode) obj).getCharTable() );
        				continue;
        			}
        		}
        		throw new Exception("Invalid production right-hand-side: expected strings or Lexer.Node instances only");
        	}
        	ProductionRHS rhs=new ProductionRHS(elements);
        	productions.store(name, rhs);

        	return new ValueObj(theObj());
        }
    }
    
    private FrontEnd frontEnd;
    
    
    class FunctionUseFrontEndAdHoc extends Function {
        public String getName() {
            return "useFrontEndAdHoc";
        }
        public String getShortDesc() {
            return "useFrontEndAdHoc(line) - set up ad-hoc front-end - returns front-end object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (frontEnd != null) throw new Exception("FrontEnd already defined");
        	if (params.size() != 1) throw new Exception("Expected parameter line (String)");
        	
        	String line=getString("line",params,0);
        	ObjFrontEndAdHoc f=new ObjFrontEndAdHoc(line);
        	frontEnd=f;
        	
        	return new ValueObj(f);
        }
    }
    
    
    class FunctionUseFrontEndRegular extends Function {
        public String getName() {
            return "useFrontEndRegular";
        }
        public String getShortDesc() {
            return "useFrontEndRegular(tokenStream) - set up regular front-end - returns front-end object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (frontEnd != null) throw new Exception("FrontEnd already defined");
        	if (params.size() != 1) throw new Exception("Expected parameter tokenStream");
        	
        	Obj obj=getObj("tokenStream", params, 0);
        	if (!(obj instanceof ObjLexerTokenStream)) throw new Exception("Expected parameter tokenStream");
        	
        	ObjFrontEndRegular f=new ObjFrontEndRegular((ObjLexerTokenStream) obj);
        	frontEnd=f;
        	return new ValueObj(f);
        }
    }
    
    
    
//    
//    private Value parse (String startProd, ValueString line) throws Exception {
//     	return processProduction(startProd, state);
//    }
//    
//    /**
//     * Look up Production by name and try all alternatives in order. Returns Value or Java null if no result
//     */
//    private Value processProduction (String startProd) throws Exception {
//    	Production prod=productions.getProduction(startProd);
//    	
//    	if (prod==null) throw new Exception("Grammar error: Unknown production: " + prod);
//    	List<ProductionRHS> alternatives=prod.getAlternatives();
//    	
//    	int currPos=state.getCharSource().getPos();
//    	for (ProductionRHS rhs:alternatives) {
//    		state.getCharSource().setPos(currPos);
//    		Value v=processProductionRHS (rhs, state);
//    		if (v != null) return v;
//    	}
//    	
//    	return null;
//    }
//    
//    private Value processProductionRHS (ProductionRHS rhs) throws Exception {
//    	final int count=rhs.getElementCount();
//    	boolean ponr=false;
//
//    	return null;
////    	for (int i=0; i<count; i++) {
////    		String type=rhs.getType(i);
////    		if (type==ProductionRHS.TYP_PONR) {
////    			state.gotPONR();
////    			ponr=true;
////    			continue;
////    		}
////    		if (type==ProductionRHS.TYP_TOKEN) {
////    			CharTable t=rhs.getCharTable(i);
////    			int currPos=state.getCharSource().getPos();
////				Integer tokType = t.parse(state.getCharSource());
////				if (tokType==null) {
////					state.tokenFailed(currPos);
////					return null;
////				} 
////				return new (ValueString)
////			
////    		} else if (type==ProductionRHS.TYP_NONTERMINAL) {
////    			String prodName=rhs.getTargetProductionName(i);
////    			boolean isMulti=rhs.targetProductionIsMulti(i);
////    			boolean isOptional=rhs.targetProductionIsOptional(i);
////    			String sepProdName = rhs.getSeparatorProductionName(i); // null if no separator
////    			
////    		} else {
////    			throw new Exception("Invalid RHS element type: " + type);
////    		}
////    	}
//    	
//    }
//    
//    
//
//    
//   

}
