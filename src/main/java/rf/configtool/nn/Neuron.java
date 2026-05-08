package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;

public class Neuron {
	ActivationFunction activationFunction;

	List<Neuron> inputNeurons;
	List<Float> inputWeights;  // referring to location in output vector from previous layer
	float bias;
	float rawSum;
	float activation;
	float delta;

	// random init
	public Neuron (List<Neuron> inputNeurons, ActivationFunction activationFunction) {
		this.inputNeurons=inputNeurons;
		this.activationFunction=activationFunction;

		inputWeights=new ArrayList<Float>();
		for (int i=0; i<inputNeurons.size(); i++) {
			inputWeights.add(activationFunction.randomWeight());
		}
		bias=activationFunction.randomBias();
	}


	public float forward () {
		if (inputNeurons.size()==0) {
			rawSum=bias;
			activation=bias;
			return activation;
		}
		rawSum=bias;
		for (int i=0; i<inputWeights.size(); i++) {
			rawSum += (inputWeights.get(i)*inputNeurons.get(i).activation);
		}

		activation=activationFunction.activation(rawSum);
		return activation;
	}
	
	public float derivative() {
		return activationFunction.derivative(rawSum, activation);
	}

	
}
