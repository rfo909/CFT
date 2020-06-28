package rf.configtool.main.runtime.lib.conversions;

public class ConvAdd extends Conv {
    private String name;
    private double value;
    public ConvAdd (String name, double value) {
        super(name);
        this.value=value;
    }
    public double getResult (double x) {
        return x+value;
    }
}
