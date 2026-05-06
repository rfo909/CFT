package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;

public class Brain {

	private int inputWidth;
	List<Layer> layers=new ArrayList<Layer>();

	public Brain (int inputWidth, int hiddenTiers, int hiddenTierWidth, int outputWidth) {
		this.inputWidth=inputWidth;

		ParamGenerator pgen = new ParamGenerator();
		ActivationFunction activationFunction = new ActivationSigmoid(pgen);

		// input layer
		layers.add(new Layer(inputWidth, 0, activationFunction));

		for (int i=0; i<hiddenTiers; i++) {
			int layerInputCount = (i==0 ? inputWidth : hiddenTierWidth);
			layers.add(new Layer(hiddenTierWidth, layerInputCount, activationFunction));
		}

		// output layer
		layers.add(new Layer(outputWidth, hiddenTierWidth, activationFunction));

		System.out.println("Brain parameters: " + pgen.getParamCount());

	}

	public List<Float> forwardPass (List<Float> dataVector) {
		// copy dataVector as bias values into the first layer

		if (dataVector.size() != inputWidth) throw new RuntimeException("input vector mismatch");

		List<Neuron> inputNeurons=layers.get(0).getNeurons();
		for (int i=0; i<inputNeurons.size(); i++) {
			inputNeurons.get(i).setBias(dataVector.get(i));
		}

		dataVector=new ArrayList<Float>(); // first layer has no inputs

		for (Layer layer:layers) {
			dataVector = layer.process(dataVector);
		}

		return dataVector; // result from output neurons
	}

	private List<Float> calculateOutputLayerErrors(Layer outputLayer, List<Float> correctResult) {
		List<Float> activations = outputLayer.getActivations();
		if (correctResult.size() != activations.size()) throw new RuntimeException("lossFunction: output mismatch");

		List<Float> errors=new ArrayList<Float>();
		for (int i=0; i<activations.size(); i++) {
			float dx=activations.get(i)-correctResult.get(i);
			errors.add(dx*dx);
		}

		return errors;
    }



	/*
	Call this method after a regular execute(), having produced output values (activations). These
	are stored in the layers, as well as the rawSum stored in each Neuron
	*/
	public void backPropagate (float learningRate, List<Float> target) {
		List<Float> outputErrors=calculateOutputLayerErrors(layers.get(layers.size()-1), target);

		int numLayers=layers.size();

	}
}
