package rf.configtool.nn;

import java.util.ArrayList;
import java.util.Random;

public class MutationState {
	
	private ArrayList<Mutation> mutations=new ArrayList<Mutation>();
	private Random random;
	
	public MutationState() {
		random=new Random(System.currentTimeMillis());
	}
	
	// -----------------------------------------------
	// Random numbers
	// -----------------------------------------------
	public int getRandomInt (int bound) {
		return random.nextInt(bound);
	}
	
	public double getRandomDouble (double bound) {
		return random.nextDouble() * bound;
	}
	
	public double getRandomDouble () {
		return random.nextDouble();
	}
	
	// -----------------------------------------------
	// Mutations
	// -----------------------------------------------
	
	public void addMutation (Mutation m) {
		mutations.add(m);
	}
	
	public void rollback() {
		// undo mutations in reverse order, which means the exploration tree can be any depth
		for (int pos=mutations.size()-1; pos >=0; pos--) {
			mutations.get(pos).rollback();
		}
		mutations.clear();
	}
	
	public void confirmMutations() {
		mutations.clear(); // no rollback
	}

	public int getMutationCount() {
		return mutations.size();
	}
	
}
