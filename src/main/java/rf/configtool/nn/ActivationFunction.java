package rf.configtool.nn;

public abstract class ActivationFunction {
    ParamGenerator paramGenerator;

    public ActivationFunction (ParamGenerator paramGenerator) {
        this.paramGenerator=paramGenerator;
    }
    public abstract float activation (float rawSum);
    public abstract float derivative (float rawSum, float activation);

    public abstract float randomWeight();
    public abstract float randomBias();
}
