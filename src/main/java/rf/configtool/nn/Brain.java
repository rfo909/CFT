package rf.configtool.nn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Brain {

	private int inputWidth;
	List<Layer> layers=new ArrayList<Layer>();

	public Brain (int inputWidth, int hiddenTiers, int hiddenTierWidth, int outputWidth) {
		this.inputWidth=inputWidth;
		Random random=new Random();

		for (int i=0; i<hiddenTiers; i++) {
			int layerInputCount = (i==0 ? inputWidth : hiddenTierWidth);
			layers.add(new Layer(hiddenTierWidth, layerInputCount, random));
		}

		// output layer
		layers.add(new Layer(outputWidth, hiddenTierWidth, random));

	}

	public List<Float> execute (List<Float> dataVector) {
		if (dataVector.size() != inputWidth) throw new RuntimeException("input vector mismatch");
	
		for (Layer layer:layers) {
			dataVector = layer.process(dataVector);
		}

		return dataVector; // result from output neurons

	}
}
