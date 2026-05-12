package rf.configtool.nn;

public class ActivationReLU extends ActivationFunction {
    // "Rectified Linear Unit"

    @Override
    public float activation(float rawSum) {
        if (rawSum < 0) return 0;
        return rawSum;
    }

    @Override
    public float derivative(float rawSum, float activation) {
        if (rawSum > 0) return 1;
        return 0;
    }

    @Override
    public float randomWeight() {
        return random(0.2f) - 0.1f;
    }

    @Override
    public float randomBias() { return random(0.1f) - 0.5f; }

}
