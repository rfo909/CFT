package rf.configtool.nn;

public class MutationConnectionWeight implements Mutation {
	
	private Connection conn;
	private double weight;
	
	public MutationConnectionWeight (Connection conn, double weight) {
		this.conn=conn;
		this.weight=weight;
	}
	
	public void rollback() {
		conn.rollbackWeight(weight);
	}

}
