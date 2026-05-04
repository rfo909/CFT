package rf.configtool.nn;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

public class ObjBrain extends Obj {
    private Brain brain;

    public ObjBrain(Brain brain) {
        this.brain=brain;
        this.add(new FunctionProcess());
        this.add(new FunctionBP());
    }

    @Override
    public boolean eq(Obj x) {
        if (x==this) return true;
        return false;
    }

    @Override
    public String getTypeName() {
        return "Brain";
    }

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("[Brain]");
    }

    private List<Float> fromCFTFormat (ValueList list) {
        List<Value> vList=list.getVal();
        List<Float> fList=new ArrayList<Float>();
        for (Value v:vList) {
            if (v instanceof ValueFloat) {
                double val=((ValueFloat) v).getVal();
                fList.add((float) val);
            } else if (v instanceof ValueInt) {
                long val=((ValueInt) v).getVal();
                fList.add((float) val);
            } else {
                throw new RuntimeException("Brain invalid data");
            }
        }
        return fList;
    }

    private ValueList toCFTFormat (List<Float> data) {
        List<Value> vList=new ArrayList<Value>();
        for (Float f:data) {
            vList.add(new ValueFloat(f));
        }
        return new ValueList(vList);
    }

    class FunctionProcess extends Function {
        public String getName() {
            return "process";
        }
        public String getShortDesc() {
            return "process(list) - process list of float, returns list of float";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected list parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueList)) throw new Exception("Expected list parameter");

            List<Float> inputs=fromCFTFormat((ValueList) value);
            
            List<Float> result = brain.execute(inputs);
            return toCFTFormat(result);
        }
    }


    class FunctionBP extends Function {
        public String getName() {
            return "BP";
        }
        public String getShortDesc() {
            return "BP(targetList) - backpropagation test";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected target list of desired values");

            Value value=params.get(0);
            if (!(value instanceof ValueList)) throw new Exception("Expected target list");

            List<Float> target=fromCFTFormat((ValueList) value);
            brain.backPropagate(target);


            return new ValueBoolean(true);
        }
    }



}