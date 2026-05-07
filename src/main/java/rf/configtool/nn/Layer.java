package rf.configtool.nn;

import java.util.*;

public class Layer {
    List<Neuron> neurons=new ArrayList<Neuron>(); 

    public Layer (int width, List<Neuron> inputNeurons, ActivationFunction activationFunction) {
        for (int i=0; i<width; i++) {
            neurons.add(new Neuron(inputNeurons, activationFunction));
        }
    }

    public void forward () {
        for (Neuron n : neurons) {
            n.forward();
        }
    }


}
