package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Brain {

	private int inputWidth;
	List<Layer> layers=new ArrayList<Layer>();

	public Brain (int inputWidth, int hiddenTiers, int hiddenTierWidth, int outputWidth) {
		this.inputWidth=inputWidth;

		ParamGenerator pgen = new ParamGenerator();
		ActivationFunction activationFunction = new ActivationSigmoid(pgen);

		// input layer
		List<Neuron> emptyNeuronList=new ArrayList<Neuron>(); // input layer has no inputs
		Layer inputLayer=new Layer(inputWidth, emptyNeuronList, activationFunction);
		layers.add(inputLayer);

		Layer prevLayer=inputLayer; 

		for (int i=0; i<hiddenTiers; i++) {
			Layer newLayer=new Layer(hiddenTierWidth, prevLayer.neurons, activationFunction);
			layers.add(newLayer);
			prevLayer=newLayer;
		}

		// output layer
		layers.add(new Layer(outputWidth, prevLayer.neurons, activationFunction));

		System.out.println("Brain parameters: " + pgen.getParamCount());

	}

	public List<Float> forwardPass (List<Float> inputs) {
		// copy dataVector as bias values into the first layer

		List<Neuron> inputNeurons=layers.get(0).neurons;
		for (int i=0; i<inputNeurons.size(); i++) {
			inputNeurons.get(i).bias=inputs.get(i);
		}

		for (Layer layer:layers) {
			layer.forward();
		}

		List<Float> output=new ArrayList<Float>();
		Layer outputLayer=layers.get(layers.size()-1);
		for (Neuron n:outputLayer.neurons) {
			output.add(n.activation);
		}
		return output;
	}


	/*
	Call this method after a regular execute(), having produced output values (activations). These
	are stored in the layers, as well as the rawSum stored in each Neuron
	*/
	public void backPropagate (float learningRate, List<Float> target) {
		Layer outputLayer=layers.get(layers.size()-1);
			
		// Calculate delta for output layer
		List<Neuron> outputNeurons=outputLayer.neurons;
		for (int i = 0; i < outputNeurons.size(); i++) {
			Neuron n = outputNeurons.get(i);

			float error = n.activation - target.get(i);

			n.delta = error * n.derivative();
		}

		// Calculate delta for hidden layers, in reverse
		for (int layerIndex = layers.size() - 2; layerIndex >= 1; layerIndex--) {
			Layer currentLayer = layers.get(layerIndex);
			Layer nextLayer = layers.get(layerIndex + 1);

			for (int i = 0; i < currentLayer.neurons.size(); i++) {
				Neuron currentNeuron = currentLayer.neurons.get(i);

				float weightedDeltaSum = 0;

				for (Neuron nextNeuron : nextLayer.neurons) {
					int inputIndex = nextNeuron.inputNeurons.indexOf(currentNeuron);
					double weight = nextNeuron.inputWeights.get(inputIndex);
	
					weightedDeltaSum += weight * nextNeuron.delta;
				}
	
				currentNeuron.delta =
					weightedDeltaSum * currentNeuron.derivative();
			}
		}

		// Update weights and bias
		for (int layerIndex = 1; layerIndex < layers.size(); layerIndex++) {
			Layer layer = layers.get(layerIndex);

			for (Neuron n : layer.neurons) {
				for (int i = 0; i < n.inputNeurons.size(); i++) {
					float inputActivation = n.inputNeurons.get(i).activation;

					float gradient = n.delta * inputActivation;

					n.inputWeights.set(i, n.inputWeights.get(i) - learningRate * gradient);
				}

				n.bias = n.bias - learningRate * n.delta;
			}
		}
	}

	public void exportToFile (File f) throws Exception {
		try (PrintStream ps=new PrintStream(new FileOutputStream(f))) {
			for (Layer layer:layers) {
				ps.println(layer.neurons.size());
			}
			ps.println();
			for (Layer layer:layers) {
				for (Neuron neuron : layer.neurons) {
					ps.println(neuron.bias);
					for (Float w:neuron.inputWeights) ps.println(w);
				}
			}
		}
	}
}
