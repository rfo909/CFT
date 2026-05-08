package rf.configtool.nn;

import java.lang.Math;
public class ActivationSigmoid extends ActivationFunction {

    @Override
    public float activation(float rawSum) {
        return (float) (1.0 / (1.0 + Math.exp(-rawSum)));
    }

    @Override
    public float derivative(float rawSum, float activation) {
        return activation * (1f-activation);
    }

    @Override
    public float randomWeight() {
        return random(0.1f);
    }

    @Override
    public float randomBias() {
        return random(0.1f);
    }
}
