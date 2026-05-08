package rf.configtool.nn;

public class ActivationLinear extends ActivationFunction {
    // (not a proper activation function for neural nets, used for input layer only)

    @Override
    public float activation(float rawSum) {
        return rawSum;
    }

    @Override
    public float derivative(float rawSum, float activation) {
        return 1;
    }

    @Override
    public float randomWeight() {
        return random(1);
    }

    @Override
    public float randomBias() {
        return random(1);
    }

}
