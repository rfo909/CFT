package rf.configtool.main.runtime.lib.conversions;

public class ConvMul extends Conv {
    private String name;
    private double factor;
    public ConvMul (String name, double factor) {
        super(name);
        this.factor=factor;
    }
    public double getResult (double x) {
        return x*factor;
    }
}
