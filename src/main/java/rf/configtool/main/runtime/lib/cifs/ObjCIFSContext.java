package rf.configtool.main.runtime.lib.cifs;

import jcifs.CIFSContext;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Obj;

public class ObjCIFSContext extends Obj {
	
	private CIFSContext context;

    /**
     * @param context
     * @throws Exception
     */
    public ObjCIFSContext (CIFSContext context) throws Exception {
    	this.context=context;
    }

    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "CIFSContext";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("CIFSContext");
    }

	public CIFSContext getContext() {
		return context;
	}
	
}