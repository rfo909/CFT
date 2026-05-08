package rf.configtool.nn;

import java.util.Random;

public abstract class ActivationFunction {

    public static Random random=new Random();

    float random(float max) {
        return (float) (random.nextDouble()*max);
    }

    float random (float min, float max) {
        return random(max-min)+min;
    }
    public abstract float activation (float rawSum);

    public abstract float derivative (float rawSum, float activation);

    public abstract float randomWeight();

    public abstract float randomBias();
}
