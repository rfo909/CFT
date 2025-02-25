package rf.configtool.nn;

import java.io.PrintStream;

public class Connection {
	
	private Neuron target;
	private double weight;
	
	public Connection (Neuron target) {
		this.target=target;
		this.weight=0.175; //0.5;
	}
	
	public void transmitSignal () {
		target.addInputValue(weight);
	}
	
	public String asJSON() {
		return("{target='" + target.getName() + "', weight=" + weight + "}");
	}

	
	public void mutate (MutationState mutations) {
		mutations.addMutation(new MutationConnectionWeight(this, weight));
		
		int what=mutations.getRandomInt(6);
		if (what==0) {
			weight=0;
		} else if (what==1 || what==2) {
			weight += mutations.getRandomDouble(0.05);
		} else {
			// majority of Connection mutations are small adjustments
			double x=0.05;
			double factor=mutations.getRandomDouble(x) - (x/2) + 1;
			// System.out.println("Factor=" + factor); 
			// OK
			
			weight = weight * factor;
		}
	}
	
	public void rollbackWeight (double weight) {
		this.weight=weight;
	}

	// -----------------------------------------------------------------------------
	// Reporting
	// -----------------------------------------------------------------------------
	
	public Neuron getTarget() {
		return target;
	}
	
	public double getWeight() {
		return weight;
	}
}
