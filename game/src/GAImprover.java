import java.util.*;

/**
 * Created by Sam on 23-Oct-17.
 */
public class GAImprover {

	private int initial_population;
	private int generations;
	private double explorefactor = 1.0/Math.sqrt(2);
	private int depth = 1;
	Random r = new Random();
	Integer count = 0;

	Map<String,Double> agentMap = new HashMap();

	ArrayList<GameSimulatorGA> gameSimulators = new ArrayList();

	public GAImprover() {
		this.initial_population = 10;
		this.generations = 25;
	}

	public GAImprover(int population, int gen, double init_exp_factor, int init_depth) {
		this.initial_population = population;
		this.generations = gen;
		this.explorefactor = init_exp_factor;
		this.depth = init_depth;
	}

	public void initialise() {
		for (int i = 0; i < initial_population; i++) {
			//Create population
			newInstance();
		}
		runGeneticImprovement();
	}

	public void newInstance() {
		depth = r.nextInt(14) + 1;
		int multiplier = r.nextInt(4)+1;
		double representation = (double)depth*100 + r.nextDouble()*multiplier;
		System.out.println(representation);
		System.out.println("Depth: " + (int)(representation/100.0) + " Exploration: " +
				""+representation%10.0);
		Agent a = new Agent("Carlo Monty");
		GameSimulatorGA game = new GameSimulatorGA(a);
		gameSimulators.add(game);
		agentMap.put(game.name,representation);
	}

	public void makeBabies(double a, double b) {
		int new_depth = Math.round((float)(((a/100.0)+(b/100.0))/2.0))*100;
		double representation = Math.round(new_depth) + ((a%10.0)+(b%10.0))/2.0; //Average
		// components
		Agent a1 = new Agent("Carlo Monty");
		GameSimulatorGA game = new GameSimulatorGA(a1);
		gameSimulators.add(game);
		agentMap.put(game.name,representation);
	}

	public void runGeneticImprovement() {
		for (int i = 0; i < generations; i ++) {
			for (GameSimulatorGA game:gameSimulators) {
				long time = System.currentTimeMillis();
				game.run();
				System.out.println("------------------------\nDONE "+" Time Taken: "+ (System.currentTimeMillis()-time) + "\n------------------------\n");
			}

			int children = gameSimulators.size()/4;
			evaluateFitness();
			if (i < generations-1) {
				crossOver(children);
				mutate();
			}
		}
		Collections.sort(gameSimulators,new GameComparator());
		int size = gameSimulators.size();
		if (size > 10) {
			for (int i = size-1; i >= 0 ; i--) {
				double rep = agentMap.get(gameSimulators.get(i).name);
				System.out.println("Agent "+i+": Depth: " + (int) (rep / 100.0) + " Exploration "  + rep %
						10.0 + " Num Wins: " + gameSimulators.get(i).getWins());
			}
		} else {
			for (int i = size-1; i >= 0 ; i--) {
				double rep = agentMap.get(gameSimulators.get(i).name);
				System.out.println("Agent "+i+": Depth: " + (int) (rep / 100.0) + " Exploration "  + rep %
						10.0 + " Num Wins: " + gameSimulators.get(i).getWins());
			}
		}
	}

	public void evaluateFitness() {
		Collections.sort(gameSimulators, new GameComparator());
		int remove_num = gameSimulators.size()/4; // remove 1 quarter percent of population
		for (int j = 0; j < remove_num; j++) gameSimulators.remove(0); // Remove worst
	}

	public void crossOver(int num_children) {
		int size = gameSimulators.size();
		//Combine best two all the way down.
		for (int i = 0; i < num_children*2; i+=2) {
			makeBabies(agentMap.get(gameSimulators.get(i).name),agentMap.get(gameSimulators.get(i+1).name));
		}
	}

	public void mutate() {
		int randSize = r.nextInt(gameSimulators.size()/2); // Random number of atleast half population affected
		for (int i = 0; i < randSize; i++) {
			int k = r.nextInt(randSize);
			double rep = agentMap.get(gameSimulators.get(k).name);
			int pos_neg = r.nextInt(1);
			if (pos_neg == 0) pos_neg = -1;
			double variate = (double)(pos_neg)*r.nextDouble()/10.0; //max 0.1 change this is meant to
			// fine tune it
			int depth = (int)rep/100;
			double exp_factor = rep%10.0 + variate;
			agentMap.put(gameSimulators.get(k).name,(double)depth+exp_factor);
			System.out.println("Mutated: " + rep+ " To " + ((double)depth + exp_factor));
		}
	}

	public static void main(String[] args) {
		GAImprover gaImprover = new GAImprover();
		long time = System.currentTimeMillis();
		gaImprover.initialise();
		System.out.println("Time Taken: "+ (System.currentTimeMillis()-time));
	}

}

class GameComparator implements Comparator<GameSimulatorGA> {

	@Override
	public int compare(GameSimulatorGA a, GameSimulatorGA b) {
			return Integer.compare(a.getWins(), b.getWins()); // reverse
	}

}