package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Neuron {
	private List<Float> inputWeights;  // referring to location in output vector from previous layer

	public Neuron (int previousLayerWidth, Random random) {
		inputWeights=new ArrayList<Float>();
		for (int i=0; i<previousLayerWidth; i++) {
			inputWeights.add((float) (random.nextFloat()*2.0f - 1.0f));
		}
	}

	public float processInputVector (List<Float> inputs) {
		float sum=0.0f;

		if (inputWeights.size() != inputs.size()) {
			throw new RuntimeException("Weight vs input mismatch");
		}

		for (int i=0; i<inputWeights.size(); i++) {
			sum += (inputWeights.get(i)*inputs.get(i));
		}

		// ReLU activation function

		if (sum<0) {
			return 0.0f;
		} else {
			return sum;
		}

	}

	
}
