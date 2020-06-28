package rf.configtool.main.runtime.lib.conversions;


public class ConvFahrenheitCelsius extends Conv {
    private String name;
    public ConvFahrenheitCelsius () {
        super("fahrenheitToCelsius");
    }
    public double getResult (double x) {
        return (x-32)/1.8;
    }
}
