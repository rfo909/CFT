package rf.configtool.main.runtime.lib.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Single production symbol data - may be multiple alternatives
 */
public class Production {
	
	private List<ProductionRHS> alternatives=new ArrayList<ProductionRHS>();
	
	public Production (ProductionRHS rhs) {
		alternatives.add(rhs);
	}
	
	public void addAlternative (ProductionRHS alt) {
		alternatives.add(alt);
	}
	public List<ProductionRHS> getAlternatives() {
		return alternatives;
	}
	
}
