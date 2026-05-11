package rf.configtool.nn;

public class ErrorFuncMeanAbsolute extends ErrorFunc {

    public float error(float activation, float target) {
        return Math.abs(activation-target);
    }

    public float derivative (float activation, float target) {
        float f=activation-target;
        if (f<0) return -1f;
        if (f>0) return 1f;
        return 0f;
    }
}
