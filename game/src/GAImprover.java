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

	Map<String,Double> GAMap = new HashMap();
	Map<GameSimulatorGA,String> agentMap = new HashMap();

	ArrayList<GameSimulatorGA> gameSimulators = new ArrayList();

	public GAImprover() {
		this.initial_population = 9;
		this.generations = 2;
	}

	public GAImprover(int population, int gen, double init_exp_factor, int init_depth) {
		this.initial_population = population;
		this.generations = gen;
		this.explorefactor = init_exp_factor;
		this.depth = init_depth;
	}

	public String generateName() {
		StringBuilder name = new StringBuilder("");
		char[] alpha = new char[] {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',
				'q'};
		for (int j = 0; j < 20; j++) {
			name.append(alpha[r.nextInt(alpha.length)]);
		}
		return name.toString();
	}

	public void initialise() {
		for (int i = 0; i < initial_population; i++) {
			//Create population
			newInstance();
		}
		runGenticImprovement();
	}

	public void newInstance() {
		depth = r.nextInt(14) + 1;
		int multiplier = r.nextInt(5)+1;
		double representation = (double)depth*100 + r.nextDouble()*multiplier;
		System.out.println(representation);
		System.out.println("Depth: " + (int)(representation/100.0) + " Exploration: " +
				""+representation%10);
		String name = generateName();
		GAMap.put(name,representation);
		Agent a = new Agent();
		GameSimulatorGA game = new GameSimulatorGA(a);
		gameSimulators.add(game);
		agentMap.put(game,name);
	}

	public void makeBabies(double a, double b) {
		double representation = (a+b)/2.0; //Average
		String name = generateName();
		GAMap.put(name,representation);
		Agent a1 = new Agent();
		GameSimulatorGA game = new GameSimulatorGA(a1);
		gameSimulators.add(game);
		agentMap.put(game,name);
	}

	public void runGenticImprovement() {
		for (int i = 0; i < generations; i ++) {
			for (GameSimulatorGA game:gameSimulators) {
				game.run();
				System.out.println("--------------------\nDONE\n------------------------\n");
			}
			evaluateFitness();
			crossOver();
			mutate();
		}
		Collections.sort(gameSimulators,new GameComparator());
		for (int i = 0; i < 10; i++) {
			double rep = GAMap.get(agentMap.get(gameSimulators.get(i)));
			System.out.println("Agent 1: " + (int)(rep/100.0) + rep%10.0);
		}
	}

	public void evaluateFitness() {
		Collections.sort(gameSimulators, new GameComparator());
		int remove_num = gameSimulators.size()/3; // remove 1 third percent of population
		for (int j = 0; j < remove_num; j++) gameSimulators.remove(gameSimulators.size()-1); // Remove worst
	}

	public void crossOver() {
		int size = gameSimulators.size();
		//Combine best two all the way down.
		for (int i = 0; i < size; i+=2) {
			makeBabies(GAMap.get(agentMap.get(gameSimulators.get(i))),GAMap.get(agentMap.get
					(gameSimulators.get(i+1))));
		}
	}

	public void mutate() {


	}

	public static void main(String[] args) {
		GAImprover gaImprover = new GAImprover();
		gaImprover.initialise();
	}

}

class GameComparator implements Comparator<GameSimulatorGA> {

	@Override
	public int compare(GameSimulatorGA a, GameSimulatorGA b) {
			return -1*Integer.compare(a.getWins(), b.getWins()); // reverse
	}

}