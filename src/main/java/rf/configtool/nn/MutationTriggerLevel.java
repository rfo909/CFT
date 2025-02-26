package rf.configtool.nn;

public class MutationTriggerLevel implements Mutation {

	private Neuron n;
	private double triggerLevel;
	
	public MutationTriggerLevel (Neuron n, double triggerLevel) {
		this.n=n;
		this.triggerLevel=triggerLevel;
	}
	
	public void rollback() {
		n.rollbackTriggerLevel(triggerLevel);
	}
	

}
