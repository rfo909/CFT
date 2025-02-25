package rf.configtool.nn;

public interface Mutation {
	
	/**
	 * If a set of mutations did not produce a better brain, then roll them back
	 */
	public void rollback();

}
