package rf.configtool.nn;

import java.util.*;

public class Layer {
    List<Neuron> neurons=new ArrayList<Neuron>(); 

    public Layer (int width, List<Neuron> inputNeurons, ActivationFunction activationFunction) {
        for (int i=0; i<width; i++) {
            neurons.add(new Neuron(inputNeurons, activationFunction));
        }
    }

    public List<Float> process (List<Float> inputs) {
        List<Float> result=new ArrayList<Float>();
        for (Neuron n : neurons) {
            result.add(n.processInputs(inputs));
        }
        return result;
    }


}
