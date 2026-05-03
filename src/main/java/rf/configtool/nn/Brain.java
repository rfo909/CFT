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
		// copy dataVector as bias values into the first layer

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

	private float lossFunction(List<Float> output, List<Float> correctResult) {
        // Mean squared error loss function for multi-class classification
        float sum = 0f;
        
        for (int i = 0; i < correctResult.size(); i++) {
			sum += (float) Math.pow(output.get(i) - correctResult.get(i), 2);
        }
        
        return sum / correctResult.size();
    }




	public void backPropagate (List<Float> activations, List<Float> correctResult) {
		float error=lossFunction(activations, correctResult);

		/*
		// (COMPILES BUT LOOKS SHADY - weight gradient is a list in code below)

		Layer lastLayer=layers.get(layers.size()-1);
		List<Neuron> lastLayerNeurons=lastLayer.getNeurons();

		for (int i = 0; i < lastLayerNeurons.size(); i++) {
            Neuron neuron = lastLayerNeurons.get(i);
            float dz = activations.get(i); // Get the z value for backpropagation
            float dw = LEARNING_RATE * dz * error; // Compute the gradient with respect to the weights
            
            neuron.setWeightGradient(dw); // Store the weight gradient for update
            neuron.setBiasGradient(dw); // Use the same gradient for bias update

		}
		*/

		/*
		int numLayers=layers.size();

        // Backpropagate gradients through the previous layers
        for (int l = numLayers - 1; l > 0; l--) {
            Layer currentLayer = layers.get(l);
            Layer previousLayer = layers.get(l + 1);
            
            // Compute gradients for each neuron in the current layer
            List<Neuron> currentLayerNeurons = currentLayer.getNeurons();
			List<Float> prevActivations = previousLayer.getActivations();
			
            for (int i = 0; i < currentLayerNeurons.size(); i++) {
                Neuron neuron = currentLayerNeurons.get(i);

                float dz = activations.get(activations.size() - prevActivations.size() + i).getValue()[0]; // Get the z value for backpropagation
                
                // Compute the gradient with respect to the weights
                List<Float> dw = new ArrayList<>();
                for (float w : neuron.getWeights()) {
                    float dW = learningRate * dz * previousLayer.get(i).getLastOutput();
                    dw.add(dW);
                }
                
                // Update the weight gradients for this layer's neurons
                neuron.setWeightGradients(dw);
                
                // Compute the gradient with respect to the bias
                float db = learningRate * dz;
                neuron.setBiasGradient(db);
            }
        }
        
        // Update weights and biases based on gradients
        for (Layer layer : layers) {
            for (Neuron neuron : layer.getNeurons()) {
                List<Float> weightGrads = neuron.getWeightGradients();
                List<Float> weights = neuron.getWeights();
                
                // Update weights
                for (int i = 0; i < weights.size(); i++) {
                    weights.set(i, weights.get(i) - weightGrads.get(i));
                }
                
                // Update bias
                float bias = neuron.getBias();
                bias -= learningRate * neuron.getBiasGradient();
                neuron.setBias(bias);
            }
        }		
		*/
	}
}
