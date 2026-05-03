package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;

public class Brain {

	private int inputWidth;
	List<Layer> layers=new ArrayList<Layer>();

	public Brain (int inputWidth, int hiddenTiers, int hiddenTierWidth, int outputWidth) {
		this.inputWidth=inputWidth;
		ParamGenerator pgen = new ParamGenerator();

		for (int i=0; i<hiddenTiers; i++) {
			int layerInputCount = (i==0 ? inputWidth : hiddenTierWidth);
			layers.add(new Layer(hiddenTierWidth, layerInputCount, pgen));
		}

		// output layer
		layers.add(new Layer(outputWidth, hiddenTierWidth, pgen));

		System.out.println("Brain parameters: " + pgen.getParamCount());

	}

	public List<Float> execute (List<Float> dataVector) {
		if (dataVector.size() != inputWidth) throw new RuntimeException("input vector mismatch");
	
		for (Layer layer:layers) {
			dataVector = layer.process(dataVector);
		}

		return dataVector; // result from output neurons
	}

	/**
	 * All the neurons of the individual layers remember their internal sum
	 * prior to the activation function. These are used to adjust weights up
	 * the layers, to try to improve the output
	 */
	public void backPropagate (List<Float> correctResult) {

	}
}
