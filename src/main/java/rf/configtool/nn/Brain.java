package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Random;

public class Brain {

	public static Random random=new Random();
	private int inputWidth;
	List<Layer> layers=new ArrayList<Layer>();

	private ErrorFunc errorFunction = new ErrorFuncMeanAbsolute();
	private float[] outputDeltas;



	public Brain (List<Integer> layerWidths) {
		this.inputWidth=layerWidths.get(0);

		// input layer
		List<Neuron> prevLayerNeurons=new ArrayList<Neuron>(); // input layer has no inputs

		for (int i=0; i<layerWidths.size(); i++) {
			int w=layerWidths.get(i);
			ActivationFunction f;
			if (i==0) {
				// input layer uses this for rawSum pass-through
				f = new ActivationLinear();
			} else if (i==layerWidths.size()-1) {
				// output layer default, for 0-1 normalization
				f=new ActivationSigmoid();
			} else {
				// default for the other layers
				f = new ActivationLeakyReLU();
			}

			Layer newLayer=new Layer(w, prevLayerNeurons, f);
			prevLayerNeurons=newLayer.neurons;
			layers.add(newLayer);
		}

		outputDeltas=new float[layers.get(layers.size()-1).neurons.size()];
		clearOutputDeltas();
	}

	void setActivationFunction (int layer, ActivationFunction f) {
		layers.get(layer).setActivationFunction(f);
	}

	void setErrorFunction (ErrorFunc errorFunction) {
		this.errorFunction=errorFunction;
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


	// In order to process batches of data, we sum up the delta for each
	// sample, then calculate average in the backPropagate

	public void clearOutputDeltas() {
		for (int i = 0; i < outputDeltas.length; i++) outputDeltas[i] = 0f;
	}

	public void addOutputDeltas (List<Float> target) {
		Layer outputLayer=layers.get(layers.size()-1);

		// Calculate delta for output layer
		List<Neuron> outputNeurons=outputLayer.neurons;
		for (int i = 0; i < outputNeurons.size(); i++) {
			Neuron n = outputNeurons.get(i);

			float targetValue=target.get(i);

			float error = errorFunction.error(n.activation, targetValue);
			outputDeltas[i] += error * n.derivative() * errorFunction.derivative(n.activation, targetValue);
		}
	}

	public void calculateAverageDeltas (int batchSize) {
		for (int i=0; i<outputDeltas.length; i++) {
			outputDeltas[i] /= batchSize;
		}
	}


	/*
	Call this method after a regular execute(), having produced output values (activations). These
	are stored in the layers, as well as the rawSum stored in each Neuron
	*/
	public void backPropagate (float learningRate) {
		// insert deltas into output layer neurons
		Layer outputLayer=layers.get(layers.size()-1);
		List<Neuron> outputNeurons=outputLayer.neurons;

		if (outputDeltas.length != outputNeurons.size()) throw new RuntimeException("BP: output size fail");
			
		// Calculate delta for output layer
		for (int i=0; i<outputDeltas.length; i++) {
			outputNeurons.get(i).delta=outputDeltas[i];
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
				for (Neuron neuron : layer.neurons) {
					ps.println(neuron.bias);
					for (Float w:neuron.inputWeights) ps.println(w);
				}
			}
		}
	}

	public void importFromFile (File f) throws Exception {
		// no checks, assuming everything is okay
		try (BufferedReader br=new BufferedReader(new FileReader(f))) {
			for (Layer layer:layers) {
				for (Neuron neuron : layer.neurons) {
					neuron.bias=Float.parseFloat(br.readLine());
					List<Float> inputWeights=new ArrayList<Float>();
					for (int i=0; i<neuron.inputWeights.size(); i++) {
						inputWeights.add(Float.parseFloat(br.readLine()));
					}
					neuron.inputWeights=inputWeights;
				}
			}
		}
	}
}
