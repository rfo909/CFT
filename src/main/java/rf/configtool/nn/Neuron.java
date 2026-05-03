package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;

public class Neuron {

	private List<Float> inputWeights;  // referring to location in output vector from previous layer
	private float bias;

	private float weightGradient;
	private float biasGradient;
	
	private float rawSum;

	public Neuron (int previousLayerWidth, ParamGenerator pgen) {
		inputWeights=new ArrayList<Float>();
		for (int i=0; i<previousLayerWidth; i++) {
			inputWeights.add((float) (pgen.nextFloat(2f) - 1f));
		}
		bias=pgen.nextFloat(10f)-5f;
	}

	public void setBias (float bias) {
		this.bias=bias;
	}

	// backprop
	public void setWeightGradient (float wg) {
		weightGradient=wg;
	}

	// backprop
	public void setBiasGradient (float bg) {
		biasGradient=bg;
	}

	public List<Float> getInputWeights() {
		return inputWeights;
	}

	/**
	 * rawSum of (input * weight) for the complete input vector, plus bias, then
	 * apply acticavation function, returning the result value. Storing the
	 * signed rawSum internally for back prop pass. 
	 */
	public float processInputVector (List<Float> inputs) {
		this.rawSum=bias;

		if (inputWeights.size() != inputs.size()) {
			throw new RuntimeException("Weight vs input mismatch");
		}

		for (int i=0; i<inputWeights.size(); i++) {
			this.rawSum += (inputWeights.get(i)*inputs.get(i));
		}

		// ReLU activation function

		if (this.rawSum<0) {
			return 0f;
		} 
		return this.rawSum;
	}

	public float getRawSum() {
		return this.rawSum;
	}
}
