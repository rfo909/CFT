package rf.configtool.nn;

import java.lang.Math;
public class ActivationSigmoid extends ActivationFunction {

    public ActivationSigmoid (ParamGenerator paramGenerator) {
        super(paramGenerator);
    }

    @Override
    public float activation(float rawSum) {
        return (float) (1.0 / (1.0-Math.exp(rawSum)));
    }

    @Override
    public float derivative(float rawSum, float activation) {
        return (activation / (1-activation));
    }

    @Override
    public float randomWeight() {
        return paramGenerator.nextFloat(0.2f)-0.1f;
    }

    @Override
    public float randomBias() {
        return paramGenerator.nextFloat(0.2f)-0.1f;
    }
}
