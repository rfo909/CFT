package rf.configtool.nn;

public class InputNeuron extends Neuron {
	
	public InputNeuron (String name) {
		super(name);
	}
	
	public void setInputNormalized (double value) {
		if (value < 0) value=0;
		if (value > 1) value=1;
		accumulatedInput = (value * Neuron.VALUE_MAX);
	}
	
	public void clear() {
		// no action
	}
	

}
