package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;

public class Neuron {
	private List<Float> inputWeights;  // referring to location in output vector from previous layer
	private float bias;
	private float sum;

	public Neuron (int previousLayerWidth, ParamGenerator pgen) {
		inputWeights=new ArrayList<Float>();
		for (int i=0; i<previousLayerWidth; i++) {
			inputWeights.add((float) (pgen.nextFloat(2f) - 1f));
		}
		bias=pgen.nextFloat(10f)-5f;
	}

	/**
	 * Sum of (input * weight) for the complete input vector, then
	 * apply activation function, returning the result value
	 */
	public float processInputVector (List<Float> inputs) {
		this.sum=bias;

		if (inputWeights.size() != inputs.size()) {
			throw new RuntimeException("Weight vs input mismatch");
		}

		for (int i=0; i<inputWeights.size(); i++) {
			this.sum += (inputWeights.get(i)*inputs.get(i));
		}

		// ReLU activation function, capped at +256

		if (this.sum<0) {
			return 0f;
		}
		if (this.sum>256f) {
			return 256f;
		} 
		return this.sum;
	}

	
}
