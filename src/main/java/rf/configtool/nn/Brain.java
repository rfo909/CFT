package rf.configtool.nn;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Brain consists of anumber input neurons, some intermdiate tiers, and an output tier. Between each
 * tier there is an all-to-all connection matrix, but all connections have by default a weight of zero.
 *
 * Note that the input neurons are fully operational neurons
 * 
 * The depth of the mutation stack can be arbitrary.
 * 
 */
public class Brain {
	
	private List<InputNeuron> inputNeurons=new ArrayList<InputNeuron>(); 
	// input neurons have their internal value set before execution
	
	private List<Neuron> allNeurons=new ArrayList<Neuron>();
	
	private List<OutputNeuron> outputNeurons=new ArrayList<OutputNeuron>();  
	// regular neurons without output connections, that do not mutate, only accumulate
	
	private MutationState state;
	
	private Neuron getNeuron(String name) {
		Neuron n=new Neuron(name);
		allNeurons.add(n);
		return n;
	}
	
	private InputNeuron getInputNeuron(String name) {
		InputNeuron n=new InputNeuron(name);
		allNeurons.add(n);
		return n;
	}
	
	private OutputNeuron getOutputNeuron(String name) {
		OutputNeuron n=new OutputNeuron(name);
		allNeurons.add(n);
		return n;
	}
	
	public Brain (int inputWidth, int hiddenTiers, int hiddenTierWidth, int outputWidth) {
		
		state=new MutationState(); 
		
		// By defining the neurons in a bottom-up fashion, connecting a forward network
		// is simple. After creation, we reverse the list, creating a sequence of execution
		// that is top-down, ensuring that all processing at tier N is done before tier N+1
		
		// output tier
		List<Neuron> previousTier=new ArrayList<Neuron>();
		for (int i=0; i<outputWidth; i++) {
			OutputNeuron x=getOutputNeuron("o" + (i+1));
			outputNeurons.add(x);
			// no connections from these
			previousTier.add(x);
		}
		
		
		// hidden tiers, bottom up, each layer is connected to previousTier
		for (int i=0; i<hiddenTiers; i++) {
			List<Neuron> newTier=new ArrayList<Neuron>();

			for (int n=0; n<hiddenTierWidth; n++) {
				Neuron x=getNeuron("t" + (hiddenTiers-i) + "_" + (n+1));
				newTier.add(x);
				x.addConnections(previousTier);
			}
			
			previousTier=newTier;
		}
		
		// input tier
		for (int i=0; i<inputWidth; i++) {
			InputNeuron x=getInputNeuron("i" + (i+1));
			inputNeurons.add(x);
			x.addConnections(previousTier);
		}
		
		Collections.reverse(allNeurons);  // execution order
	}
	
	public void setInputValues(List<Double> values) {
		int pos=0;
		for (InputNeuron n : inputNeurons) {
			n.accumulatedInput = values.get(pos);
			pos++;
		}	
	}

	public List<Double> getOutputValues() {
		List<Double> result=new ArrayList<Double>();
		for (OutputNeuron n : outputNeurons) {
			result.add(n.getAccumulatedInput());
		}
		return result;
	}


	/*
	public List<InputNeuron> getInputNeurons() {
		return inputNeurons;
	}
	
	public List<OutputNeuron> getOutputNeurons() {
		return outputNeurons;
	}
	*/

	
	public int getNeuronCount() {
		return allNeurons.size();
	}


	
	// -------------------------------------------------------------------
	// Execution
	// -------------------------------------------------------------------
	
	/**
	 * Clear all accumulated input values in the neurons, except input neurons, which can only change
	 * by being explicitly set to new values. 
	 */
	public void reset() {
		for (Neuron n:allNeurons) n.clear();
	}
	
	/**
	 * Simulate each neuron once, updating the variance data with output values. 
	 */
	public void exeuteTopDown () {
		
		for (Neuron n:allNeurons) {
			n.tick();
		}
	}
	
	// -------------------------------------------------------------------
	// Mutations
	// -------------------------------------------------------------------

	/**
	 * Add some level of mutations to the brain
	 */
	public void mutate() {
		for (Neuron n:allNeurons) n.mutate(state);
	}
	
	/**
	 * Rollback all non-confirmed sets of mutations
	 */
	public void rollbackMutations() {
		state.rollback();
	}
	
	/**
	 * Confirm the current set of mutations
	 */
	public void confirmMutations() {
		state.confirmMutations();
	}
	
	public int getMutationCount() {
		return state.getMutationCount();
	}
	
	// -------------------------------------------------------------------
	// Logging state
	// -------------------------------------------------------------------
	
	public String asJSON () {
		StringBuffer sb=new StringBuffer();
		sb.append("{value_min=" + Neuron.VALUE_MIN + ", value_max=" + Neuron.VALUE_MAX + ", neurons=[");

		boolean comma=false;
		for (Neuron n:allNeurons) {
			if (comma) sb.append(",");
			sb.append(n.asJSON());
			comma=true;
		}
		sb.append("]}");
		return sb.toString();
	}
	

}
