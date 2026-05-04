package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;

public class Neuron {

	final float MAX_ACTIVATION = 1.0f;
	final float MIN_ACTIVATION = -0.5f;

	private List<Float> inputWeights;  // referring to location in output vector from previous layer
	private float bias;

	private float rawSum;
	private float activation; // rawSum after activation function

	private float desiredOutput;
	private int desiredOutputCount;

	public Neuron (int previousLayerWidth, ParamGenerator pgen) {
		inputWeights=new ArrayList<Float>();
		for (int i=0; i<previousLayerWidth; i++) {
			inputWeights.add((float) (pgen.nextFloat(2f) - 1f));
		}
		bias=pgen.nextFloat(2f)-1f;
	}

	public void setBias (float bias) {
		this.bias=bias;
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

		// prepare for BP
		this.desiredOutput=0f;
		this.desiredOutputCount=0;

		if (inputWeights.size() != inputs.size()) {
			throw new RuntimeException("Weight vs input mismatch");
		}

		for (int i=0; i<inputWeights.size(); i++) {
			this.rawSum += (inputWeights.get(i)*inputs.get(i));
		}

		// "shifted ReLU" activation function, allowing for negative
		// data, but keeping the transition point at zero, and the
		// derivative unchanged

		if (rawSum<0) {
			activation = rawSum/2f;  // derivative 0.5 and 1
		} else {
			activation = rawSum;
		}

		if (activation<MIN_ACTIVATION) activation=MIN_ACTIVATION;
		if (activation>MAX_ACTIVATION) activation=MAX_ACTIVATION;


		return this.activation;
	}

	
	// ----------------------------------
	// back propagation calculations
	// ----------------------------------

	public float getActivation() {
		return activation;
	}

	public void addDesiredOutput (float value) {
		this.desiredOutput += value;
		this.desiredOutputCount++;
	}

	public void backPropagate (Layer prevLayer) {
		float avgDesiredOutput=this.desiredOutput/this.desiredOutputCount;
		//System.out.println("avgDesiredOutput=" + avgDesiredOutput + " activation=" + this.activation);
		
		float diff = avgDesiredOutput - this.activation;
		
		List<Neuron> prevNeurons=prevLayer.getNeurons();
		for (int i=0; i<prevNeurons.size(); i++) {

			Neuron p=prevNeurons.get(i);
			float input=p.getActivation();
			float weight=inputWeights.get(i);

			// calculate a 1% change spread out across the input count
			float change = diff/inputWeights.size()/10f;
			inputWeights.set(i,weight+change);
			bias += change;
	
			// at this point we have handled 1% of the difference, now we ask the
			// neurons in the previous layer to close in on values that make
			// our new activation perfect

			p.addDesiredOutput(input+diff/inputWeights.get(i)); 
				// modify its previous output (our input) to handle the remaining 99% correction
		}
		
	}

	private float reluDerivative() {
		if (rawSum < 0) {
			return 0.5f;
 		} else {
			return 1f;
		}
    }

}
