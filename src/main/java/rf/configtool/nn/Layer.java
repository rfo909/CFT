package rf.configtool.nn;

import java.util.*;

public class Layer {
    private List<Neuron> neurons=new ArrayList<Neuron>(); 
    private List<Float> activations;

    public Layer (int width, int inputVectorSize, ParamGenerator pgen) {
        for (int i=0; i<width; i++) {
            neurons.add(new Neuron(inputVectorSize, pgen));
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
}
