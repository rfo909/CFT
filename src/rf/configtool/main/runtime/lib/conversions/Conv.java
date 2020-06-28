package rf.configtool.main.runtime.lib.conversions;

public abstract class Conv {
    private String name;
    public Conv (String name) {
        this.name=name;
    }
    public abstract double getResult (double x);
    
    public String getName() {
        return name;
    }
}
