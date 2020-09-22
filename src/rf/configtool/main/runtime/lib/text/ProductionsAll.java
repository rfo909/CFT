package rf.configtool.main.runtime.lib.text;

import java.util.HashMap;
import java.util.Map;

public class ProductionsAll {

    private Map<String,Production> productions=new HashMap<String,Production>();

    public Production getProduction (String name) {
    	return productions.get(name);
    }

    public void store (String name, ProductionRHS rhs) {
    	Production p=productions.get(name);
    	if (p==null) {
    		p=new Production(rhs);
    		productions.put(name, p);
    	} else {
    		p.addAlternative(rhs);
    	}
    }
}
