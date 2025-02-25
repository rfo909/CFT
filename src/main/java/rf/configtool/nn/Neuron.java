package rf.configtool.nn;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Neuron {
	public static final double VALUE_MAX = 1.0;
	public static final double VALUE_MIN = 0;

		// About variable signal strength
		// ------------------------------
		// signal strength is combined with weight of connections to receivers, and so a fixed value combined
		// with variable weights alone can produce the same results, with lesser degree of freedom
		// 2017-07-13 20:08 RFO
	
	
	private String name;
	
	protected double accumulatedInput;  // available to subclass InputNeuron
	private double triggerLevel;
	private double mutationProbability=0.5;
	
	private ArrayList<Connection> connections=new ArrayList<Connection>();
	
	public Neuron (String name) {
		this.name=name;
		accumulatedInput=0;
		triggerLevel=VALUE_MAX;
	}

	public void addConnection (Neuron n) {
		connections.add(new Connection(n));
	}
	
	public void addConnections (List<Neuron> list) {
		for (Neuron n:list) addConnection(n);
	}
	

	/**
	 * Input values from other neurons
	 */
	public void addInputValue (double value) {
		accumulatedInput += value;
	}

	public void clear() {
		accumulatedInput=VALUE_MIN;
	}
	
	public String getName() {
		return name;
	}

	/**
	 * At one point in the execution sequence, simulated time is executed for
	 * each neuron. Return true if neuron triggered
	 */
	public void tick() {
		if (accumulatedInput > VALUE_MAX) accumulatedInput=VALUE_MAX;
		if (accumulatedInput < VALUE_MIN) accumulatedInput=VALUE_MIN;

		if (isTriggering()) {
			for (Connection c:connections) {
				// signal value is 1, which means we send the connection weight
				c.transmitSignal();
			}
		}
	}
	
	public String asJSON() {
		StringBuffer sb=new StringBuffer();
		sb.append("{name='" + getName() + "', type='" + getType() + "', triggerLevel=" + triggerLevel);
		sb.append(", connections=[");
		boolean comma=false;
		for (Connection c:connections) {
			if (comma) sb.append(",");
			sb.append(c.asJSON());
			comma=true;
		}
		sb.append("]}");
		return sb.toString();
	}
	
	
	public boolean isTriggering() {
		return accumulatedInput > triggerLevel;
	}
	
	public double getAccumulatedInput() {
		return accumulatedInput;
	}
	
	// -----------------------------------------------------------------------------
	// Mutations
	// -----------------------------------------------------------------------------
	
	public void mutate (MutationState mutations) {
		if (mutations.getRandomDouble() > mutationProbability) {
			// don't mutate now, but increase probability a bit for next time
			mutationProbability *= 1.1;
			return;
		}
		
		// going to mutate
		mutationProbability *= 0.1; // prevent same neuron from mutating too often
		
		// decide which mutation to perform
		int type=mutations.getRandomInt(9);
		if (type==0 || type==1) {
			// modify triggerLevel
			mutations.addMutation(new MutationTriggerLevel(this,triggerLevel));
			double dx=0.1;
			triggerLevel += (mutations.getRandomDouble(dx) - (dx/2));
			if (triggerLevel < 0) triggerLevel=1;
			else if (triggerLevel > 1) triggerLevel=0;
		} else {
			if (connections.size() > 0) {
				int pos=mutations.getRandomInt(connections.size());
				if (pos < connections.size()) {
					connections.get(pos).mutate(mutations);
				}
			}
		}
		
	}
	
	public void rollbackTriggerLevel (double triggerLevel) {
		this.triggerLevel=triggerLevel;
	}
	
	// -----------------------------------------------------------------------------
	// Reporting
	// -----------------------------------------------------------------------------
	
	public String getType() {
		if (this instanceof InputNeuron) return "input";
		if (this instanceof OutputNeuron) return "output";
		return "hidden";
	}
	
//	public void displayState(PrintStream ps) {
//		ps.println("neuron {");
//		ps.println("   name='" + name + "'");
//		ps.println("   type='" + getType() + "'");
//		ps.println("   triggerLevel=" + triggerLevel);
//		for (Connection c:connections) {
//			ps.println("   connectionTo (name='" + c.getTarget().name + "', weight=" + c.getWeight() + ")");
//		}
//		ps.println("}");
//	}

	
}
