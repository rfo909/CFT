package rf.configtool.nn;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

public class ObjBrain extends Obj {
    private Brain brain;

    public ObjBrain(Brain brain) {
        this.brain=brain;
        this.add(new FunctionForwardPass());
        this.add(new FunctionBP());
        this.add(new FunctionExport());
        this.add(new FunctionImport());
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

    class FunctionForwardPass extends Function {
        public String getName() {
            return "forwardPass";
        }
        public String getShortDesc() {
            return "forwardPass(list) - process list of float, returns list of float";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected list parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueList)) throw new Exception("Expected list parameter");

            List<Float> inputs=fromCFTFormat((ValueList) value);
            List<Float> result = brain.forwardPass(inputs);
            return toCFTFormat(result);
        }
    }


    class FunctionBP extends Function {
        public String getName() {
            return "BP";
        }
        public String getShortDesc() {
            return "BP(learningFactor,targetList) - backpropagation test";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected learningFactor, targetList (correct values)");
            double learningRate = getFloat("learningFactor", params, 0);

            Value value=params.get(1);
            if (!(value instanceof ValueList)) throw new Exception("Expected target list");

            List<Float> target=fromCFTFormat((ValueList) value);
            brain.backPropagate((float) learningRate, target);


            return new ValueBoolean(true);
        }
    }

    class FunctionExport extends Function {
        public String getName() {
            return "export";
        }
        public String getShortDesc() {
            return "export(file) - save model to file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected file parameter");
            throw new Exception("Not implemented");
        }
    }


    class FunctionImport extends Function {
        public String getName() {
            return "import";
        }
        public String getShortDesc() {
            return "import(file) - load model from file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected file parameter");
            throw new Exception("Not implemented");
        }
    }



}