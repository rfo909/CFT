package rf.configtool.nn;

public class ActivationLeakyReLU extends ActivationFunction {
    // helps against neurons "dying" as with traditional ReLU (getting stuck non-activated at output 0 forever)

    public ActivationLeakyReLU (ParamGenerator paramGenerator) {
        super(paramGenerator);
    }

    public static final float NEGATIVE_FACTOR = 0.01f;
    @Override
    public float activation(float rawSum) {
        if (rawSum > 0) return rawSum;
        return rawSum*NEGATIVE_FACTOR;
    }

    @Override
    public float derivative(float rawSum, float activation) {
        if (rawSum > 0) return 1;
        return NEGATIVE_FACTOR;
    }

    @Override
    public float randomWeight() {
        return paramGenerator.nextFloat(2)-1;
    }

    @Override
    public float randomBias() {
        return paramGenerator.nextFloat(2)-1;
    }

}
