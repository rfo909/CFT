package rf.configtool.nn;

public abstract class ActivationFunction {

    float random(float max) {
        return (float) (Brain.random.nextDouble()*max);
    }
    public abstract float activation (float rawSum);

    public abstract float derivative (float rawSum, float activation);

    public abstract float randomWeight();

    public abstract float randomBias();
}
