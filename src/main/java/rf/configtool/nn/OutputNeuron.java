package rf.configtool.nn;

public class OutputNeuron extends Neuron {

	public OutputNeuron (String name) {
		super(name);
	}

	public void mutate (MutationState mutations) {
		return; // no mutations of output neurons
	}

}
