package rf.configtool.main.runtime.lib.vgy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.vgy.ObjINode.FunctionConfigure;
import rf.configtool.main.runtime.lib.vgy.ObjINode.FunctionGetConfiguration;
import rf.configtool.main.runtime.lib.vgy.ObjINode.FunctionStart;

public class ObjVGY extends Obj {

    public ObjVGY () {
    	this.add(new FunctionINode());
    	this.add(new FunctionSNode());
    	this.add(new FunctionClient());
    }

    
    
    private ObjVGY self() {
    	return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "VGY";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("VGY");
    }
    
    class FunctionINode extends Function {
        public String getName() {
            return "INode";
        }
        public String getShortDesc() {
            return "INode() - returns INode object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	return new ValueObj(new ObjINode());
        }
    }

    class FunctionSNode extends Function {
        public String getName() {
            return "SNode";
        }
        public String getShortDesc() {
            return "SNode() - returns SNode object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	return new ValueObj(new ObjSNode());
        }
    }

    class FunctionClient extends Function {
        public String getName() {
            return "Client";
        }
        public String getShortDesc() {
            return "Client() - returns Client object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	return new ValueObj(ObjClient.getInstance());
        }
    }

  
}
