package rf.configtool.nn;

public class ErrorFuncMeanSquared extends ErrorFunc {

    public float error(float activation, float target) {
        float f = (activation-target);
        return f*f*0.5f;
    }

    public float derivative(float activation, float target) {
        return activation-target;
    }
}
