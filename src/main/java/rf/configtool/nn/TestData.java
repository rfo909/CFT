package rf.configtool.nn;

import java.util.List;

/**
 * See interface TestDataManager
 * 
 * @author rfo90
 *
 */
public interface TestData {
	
	public void setNormalizedInputValues (List<InputNeuron> inputs);
	
	public double calculateScore (List<OutputNeuron> outputs);

}
