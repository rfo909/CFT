package rf.configtool.main.runtime.lib.text;

import java.util.List;

import rf.configtool.parser.CharTable;

// Production right hand side

public class ProductionRHS {
	
	public static final String TYP_TOKEN = "TYP_TOKEN";
	public static final String TYP_PONR = "TYP_PONR";
	public static final String TYP_NONTERMINAL = "TYP_NONTERMINAL";
	
	private List<Object> elements;

	public ProductionRHS (List<Object> elements) {
		this.elements=elements;
	}
	
	public int getElementCount() {
		return elements.size();
	}
	
	public String getType(int i) throws Exception {
		Object x=elements.get(i);
		if (x instanceof String) {
			if ( ((String)x).equals(ObjParser.PONR) ) return TYP_PONR;
			return TYP_NONTERMINAL;
		}
		if (x instanceof CharTable) return TYP_TOKEN;
		throw new Exception("Unknown RHS element type: " + x.getClass().getName());
	}
	
	
	// -------------------------------------------------------------------
	// Token
	// -------------------------------------------------------------------
	
	public CharTable getCharTable (int i) throws Exception {
		return (CharTable) elements.get(i);
	}
	
	// -------------------------------------------------------------------
	// Production reference
	// -------------------------------------------------------------------

	public String getTargetProductionName (int i) {
		String s=(String) elements.get(i);
		if (s.endsWith("?")) return s.substring(0,s.length()-1);
		if (s.contains("*")) return s.substring(0,s.indexOf('*'));
		if (s.contains("+")) return s.substring(0,s.indexOf('+'));
		return s;
	}
	
	public boolean targetProductionIsOptional (int i) {
		String s=(String) elements.get(i);
		return (s.endsWith("?") || s.contains("*"));
	}
	
	public boolean targetProductionIsMulti (int i) {
		String s=(String) elements.get(i);
		return  (s.contains("*") || s.contains("+"));
	}

	public String getSeparatorProductionName (int i) {
		String s=(String) elements.get(i);
		String sep="";
		if (s.contains("*")) {
			sep = s.substring(s.indexOf('*')+1);
		} else if (s.contains("+")) {
			sep = s.substring(s.indexOf('+')+1);
		}
		sep=sep.trim();
		if (sep.length()==0) return null;
		return sep;
	}

}
