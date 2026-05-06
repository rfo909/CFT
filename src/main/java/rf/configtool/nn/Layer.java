package rf.configtool.nn;

import java.util.*;

public class Layer {
    private List<Neuron> neurons=new ArrayList<Neuron>(); 
    private List<Float> activations;  // neuron outputs 

    public Layer (int width, int inputVectorSize, ActivationFunction activationFunction) {
        for (int i=0; i<width; i++) {
            neurons.add(new Neuron(inputVectorSize, activationFunction));
        }
    }

    public List<Float> process (List<Float> inputs) {
        List<Float> result=new ArrayList<Float>();
        for (Neuron n : neurons) {
            result.add(n.processInputVector(inputs));
        }
        this.activations=result;
        return result;
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }

    public List<Float> getActivations() {
        return activations;
    }

    // Calculate errors for this layer given the next layer's errors
    public float[] calculateErrors(float[] nextLayerErrors) {
        float[] errors = new float[neurons.size()];
        
        for (int i = 0; i < neurons.size(); i++) {
            errors[i] = neurons.get(i).calculateError(nextLayerErrors);
        }
        return errors;
    }


}
