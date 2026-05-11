package rf.configtool.nn;

public class ErrorFuncLinear extends ErrorFunc {
    // This implementation functionally corresponds to what ChatGPT calls "binary cross-entropy",
    // which is good for binary classification outputs in combination with Sigmoid in the output layer
    //
    // It should in theory (?) be useless as the derivative is constant, but works nicely with
    // Sigmoid outputs, and also LeakyReLU, which trains better than Sigmoid in the NN script,
    // classifying which input is the biggest (binary classification)

    public float error(float activation, float target) {
        return activation-target;
    }

    public float derivative (float activation, float target) {
        return 1.0f;
    }
}
