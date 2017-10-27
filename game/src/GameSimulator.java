import java.util.*;

/**
 * Created by Sam on 15-Oct-17.
 */
public class GameSimulator {

	public static void main(String[] args) {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));
		Map<String,Integer> gameResult;

		int rounds = 1;
		//MossSideWhist game = new MossSideWhist(new Agent(), new GreedyAgent(), new GreedyAgent());
        //System.out.println("simNumber, agent, final_score, number_rounds");
        MSWAgent a = new Agent("Carlos");
        MSWAgent b = new AgentTwo();
        MSWAgent c = new RandomAgent();

		System.out.println("Number of sims: " + 10);
		System.out.println("Number of games per sim: " + 10);
		System.out.println("Number of rounds per game: " + 5);
		System.out.println(a.sayName() + ",\t" + b.sayName() + ",\t" + c.sayName());
        for (int i = 0; i < 10; i++) {
        	playGameManyTimes(2, rounds, a,b,c,0.0);
		}
		Agent a_convert = (Agent)a;
		for (int i = 0; i < 16; i++) {
			System.out.println(a_convert.trickToIteration[i]);
		}
}

	private static String playGame(int nGames, MSWAgent a, MSWAgent b, MSWAgent c) {
	    Map<String, Integer> gameResult;
    	MossSideWhist game = new MossSideWhist(a,b,c);
		game.playGame(nGames, System.out);
		gameResult =  game.getScores();
		//System.out.println(game.numDraw);
		return getWinner(gameResult);
    }

    private static void playGameManyTimes(int numberOfSimulations, int nGames,
                                          MSWAgent a, MSWAgent b, MSWAgent c, double exp) {
		Map<String, Integer> wins = new HashMap<>();
		wins.put(a.sayName(), 0);
		wins.put(b.sayName(), 0);
		wins.put(c.sayName(), 0);
	    for (int i = 0; i < numberOfSimulations; i++) {
	        String w = playGame(nGames, a, b, c);
	        wins.put(w, wins.get(w) + 1);
        }
	    	System.out.println(wins.get(a.sayName()) + ",\t" +
					wins.get(b.sayName()) + ",\t" +
					wins.get(c.sayName()) + ",\t");
    }

    private static String getWinner(Map<String, Integer> scores) {
	    int max = Collections.max(scores.values());
	    for (String s : scores.keySet()) {
	        if (scores.get(s) == max) return s; // doesn't consider draws.
        }
        return null;
    }
}
