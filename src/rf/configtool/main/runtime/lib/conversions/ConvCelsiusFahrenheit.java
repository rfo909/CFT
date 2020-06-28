package rf.configtool.main.runtime.lib.conversions;

public class ConvCelsiusFahrenheit extends Conv {
    private String name;
    public ConvCelsiusFahrenheit () {
        super("celsiusToFahrenheit");
    }
    public double getResult (double x) {
        return x*1.8+32;
    }
}
