package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;

public class Brain {

	static final float LEARNING_RATE = 0.01f; 

	private int inputWidth;
	List<Layer> layers=new ArrayList<Layer>();

	public Brain (int inputWidth, int hiddenTiers, int hiddenTierWidth, int outputWidth) {
		this.inputWidth=inputWidth;
		ParamGenerator pgen = new ParamGenerator();

		// input layer
		layers.add(new Layer(inputWidth, 0, pgen));

		for (int i=0; i<hiddenTiers; i++) {
			int layerInputCount = (i==0 ? inputWidth : hiddenTierWidth);
			layers.add(new Layer(hiddenTierWidth, layerInputCount, pgen));
		}

		// output layer
		layers.add(new Layer(outputWidth, hiddenTierWidth, pgen));

		System.out.println("Brain parameters: " + pgen.getParamCount());

	}

	public List<Float> execute (List<Float> dataVector) {
		// copy dataVector as bias values into the first layer; those neurons have zero inputs, 
		// so the bias becomes their activation value

		if (dataVector.size() != inputWidth) throw new RuntimeException("input vector mismatch");

		List<Neuron> inputNeurons=layers.get(0).getNeurons();
		for (int i=0; i<inputNeurons.size(); i++) {
			inputNeurons.get(i).setBias(dataVector.get(i));
		}

		dataVector=new ArrayList<Float>(); // empty list of inputs for first layer 
	
		for (Layer layer:layers) {
			dataVector = layer.process(dataVector);
		}

		return dataVector; // result from output neurons
	}


	// ---------------------------------------------------
	// Back propagation
	// ---------------------------------------------------

	private void setOutputCorrectValues (List<Float> correctResult) {
		Layer outputLayer=layers.get(layers.size()-1);
		List<Neuron> neurons=outputLayer.getNeurons();
		if (correctResult.size() != neurons.size()) throw new RuntimeException("output mismatch");
		for (int i=0; i<neurons.size(); i++) {
			neurons.get(i).addDesiredOutput(correctResult.get(i));
		}
    }

	/*
	Call this method after a regular execute(), having produced output values
	*/
	public void backPropagate (List<Float> target) {
		setOutputCorrectValues(target);

		for (int layer=layers.size()-1; layer>=1; layer--) {
			Layer currLayer=layers.get(layer);
			Layer prevLayer=layers.get(layer-1);  // for looking up their previous output

			for (Neuron n:currLayer.getNeurons()) {
				n.backPropagate(prevLayer);
			}
		}
	}
}
