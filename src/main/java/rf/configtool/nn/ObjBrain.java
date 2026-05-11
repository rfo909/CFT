package rf.configtool.nn;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjFile;
import java.io.*;

public class ObjBrain extends Obj {
    private Brain brain;

    public ObjBrain(Brain brain) {
        this.brain=brain;

        this.add(new FunctionUseReLU());
        this.add(new FunctionUseLeakyReLU());
        this.add(new FunctionUseSigmoid());
        this.add(new FunctionUseLinear());

        this.add(new FunctionErrorMeanSquared());
        this.add(new FunctionErrorMeanAbsolute());
        this.add(new FunctionErrorLinear());

        this.add(new FunctionForwardPass());

        this.add(new FunctionClearOutputDeltas());
        this.add(new FunctionAddOutputDeltas());

        this.add(new FunctionCalculateAverageDeltas());
        this.add(new FunctionBackPropagate());
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




    class FunctionUseReLU extends Function {
        public String getName() {
            return "useReLU";
        }
        public String getShortDesc() {
            return "useReLU(layer) - set ReLU activation function for layer";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected layer id (int)");
            int layer=(int) getInt("layer", params, 0);
            brain.setActivationFunction(layer, new ActivationReLU());
            return new ValueNull();
        }
    }

    class FunctionUseLeakyReLU extends Function {
        public String getName() {
            return "useLeakyReLU";
        }
        public String getShortDesc() {
            return "useLeakyReLU(layer) - set activation function for layer";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected layer (int)");
            int layer=(int) getInt("layer", params, 0);
            brain.setActivationFunction(layer, new ActivationLeakyReLU());
            return new ValueNull();
        }
    }

    class FunctionUseSigmoid extends Function {
        public String getName() {
            return "useSigmoid";
        }
        public String getShortDesc() {
            return "useSigmoid(layer) - set activation function for layer";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected layer (int)");
            int layer=(int) getInt("layer", params, 0);
            brain.setActivationFunction(layer, new ActivationSigmoid());
            return new ValueNull();
        }
    }

    class FunctionUseLinear extends Function {
        public String getName() {
            return "useLinear";
        }
        public String getShortDesc() {
            return "useLinear(layer) - set activation function for layer";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected layer (int)");
            int layer=(int) getInt("layer", params, 0);
            brain.setActivationFunction(layer, new ActivationLinear());
            return new ValueNull();
        }
    }


    class FunctionErrorMeanSquared extends Function {
        public String getName() {
            return "errorMeanSquared";
        }
        public String getShortDesc() {
            return "errorMeanSquared() - set error function";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            brain.setErrorFunction(new ErrorFuncMeanSquared());
            return new ValueNull();
        }
    }

    class FunctionErrorMeanAbsolute extends Function {
        public String getName() {
            return "errorMeanAbsolute";
        }
        public String getShortDesc() {
            return "errorMeanAbsolute() - set error function";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            brain.setErrorFunction(new ErrorFuncMeanAbsolute());
            return new ValueNull();
        }
    }

    class FunctionErrorLinear extends Function {
        public String getName() {
            return "errorLinear";
        }
        public String getShortDesc() {
            return "errorLinear() - set error function";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            brain.setErrorFunction(new ErrorFuncLinear());
            return new ValueNull();
        }
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


    class FunctionClearOutputDeltas extends Function {
        public String getName() {
            return "clearOutputDeltas";
        }
        public String getShortDesc() {
            return "clearOutputDeltas() - reset list of deltas for output neurons";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");

            brain.clearOutputDeltas();

            return new ValueBoolean(true);
        }
    }



    class FunctionAddOutputDeltas extends Function {
        public String getName() {
            return "addOutputDeltas";
        }
        public String getShortDesc() {
            return "addOutputDeltas(targetList) - add to list of deltas for output neurons";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected targetList (correct output values)");
            Value value=params.get(0);
            if (!(value instanceof ValueList)) throw new Exception("Expected targetList");

            List<Float> target=fromCFTFormat((ValueList) value);
            brain.addOutputDeltas(target);

            return new ValueBoolean(true);
        }
    }


    class FunctionCalculateAverageDeltas extends Function {
        public String getName() {
            return "calculateAverageDeltas";
        }
        public String getShortDesc() {
            return "calculateAverageDeltas(batchSize) - average deltas across a batch";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected batchSize parameter");
            int batchSize=(int) getInt("batchSize",params,0);

            brain.calculateAverageDeltas(batchSize);

            return new ValueBoolean(true);
        }
    }



    class FunctionBackPropagate extends Function {
        public String getName() {
            return "BackPropagate";
        }
        public String getShortDesc() {
            return "BackPropagate(learningFactor) - backpropagation using the averaged output deltas";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected learningFactor parameter");
            double learningRate = getFloat("learningFactor", params, 0);
            brain.backPropagate((float) learningRate);

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
            Obj obj=getObj("file", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected file parameter");
            File f=((ObjFile) obj).getFile();
            brain.exportToFile(f);
            return new ValueNull();
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
            Obj obj=getObj("file", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected file parameter");
            File f=((ObjFile) obj).getFile();
            brain.importFromFile(f);
            return new ValueNull();
        }
    }



}