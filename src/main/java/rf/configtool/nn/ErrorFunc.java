package rf.configtool.nn;

public abstract class ErrorFunc {

    public abstract float error(float activation, float target);

    public abstract float derivative(float activation, float target);
}
