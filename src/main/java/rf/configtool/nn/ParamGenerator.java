package rf.configtool.nn;

import java.util.Random;

public class ParamGenerator {

    private Random random=new Random();
    private long count=0;

    public float nextFloat (float max) {
        count++;
        return random.nextFloat()*max;
    }

    public long getParamCount() {
        return count;
    }

}
