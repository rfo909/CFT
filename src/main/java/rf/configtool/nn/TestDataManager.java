package rf.configtool.nn;

import java.util.List;

/**
 * Experimental generic handling of
 * - config of brain
 * - training data and evaluation
 * 
 * For some generic version of PrimeTrainer1.java
 * 
 * 
 * @author rfo90
 *
 */
public interface TestDataManager {
	
	public double getMaxScore();
	
	public int getInputValueCount();
	
	public int getOutputValueCount();
	
	public List<TestData> getTestData();
	

}
