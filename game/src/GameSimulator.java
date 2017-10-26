import java.util.*;

/**
 * Created by Sam on 15-Oct-17.
 */
public class GameSimulator {

	public static void main(String[] args) {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));
		Map<String,Integer> gameResult;

		int rounds = 3;
		//MossSideWhist game = new MossSideWhist(new Agent(), new GreedyAgent(), new GreedyAgent());
        //System.out.println("simNumber, agent, final_score, number_rounds");
        MSWAgent b = new RandomAgent();
		MSWAgent c = new RandomAgent();
        int NUMBEROFSIMS = 50;
        int NUMBEROFGAMES = 10;
        System.out.println("Number of simulations: " + NUMBEROFSIMS);
        System.out.println("Number of games: " + NUMBEROFGAMES);
        System.out.println("Number of rounds per game: " + rounds);
		for (double exp = 0.25; exp < 2.50; exp += 0.25) {
			MSWAgent a = new AgentTwo(exp);
		// 100 simulations; each simulation has 10 games (and each game has 3 rounds).
			System.out.println(a.sayName() + ",\t" + b.sayName() + ",\t" + c.sayName() + ",\texploration");
			for (int i = 0; i < NUMBEROFSIMS; i++) {
				playGameManyTimes(NUMBEROFGAMES, rounds, a, b, c, exp);
			}
		}
}

	private static String playGame(int nGames, MSWAgent a, MSWAgent b, MSWAgent c) {
	    Map<String, Integer> gameResult;
    	MossSideWhist game = new MossSideWhist(a,b,c);
		game.playGame(nGames, System.out);
		gameResult =  game.getScores();
		//System.out.println(game.numDraw);
		for (String s:gameResult.keySet()) {
//            System.out.printf("%s, %d, %d\n", s, gameResult.get(s), nGames);
		}
		//System.out.println("--------");
        //System.out.println(getWinner(gameResult) + " won\n");
		return getWinner(gameResult);
    }

    private static void playGameManyTimes(int numberOfSimulations, int nGames,
                                          MSWAgent a, MSWAgent b, MSWAgent c, double exp) {
		Map<String, Integer> wins = new HashMap<>();
		wins.put(a.sayName(), 0);
		wins.put(b.sayName(), 0);
		wins.put(c.sayName(), 0);
//		System.out.println(a.sayName() + ",\t\t" + b.sayName() + ",\t\t" + c.sayName());
	    for (int i = 0; i < numberOfSimulations; i++) {
	        String w = playGame(nGames, a, b, c);
	        wins.put(w, wins.get(w) + 1);
        }
	    	System.out.println(wins.get(a.sayName()) + ",\t" +
					wins.get(b.sayName()) + ",\t" +
					wins.get(c.sayName()) + ",\t" + exp);
    }

    private static String getWinner(Map<String, Integer> scores) {
	    int max = Collections.max(scores.values());
	    for (String s : scores.keySet()) {
	        if (scores.get(s) == max) return s; // doesn't consider draws.
        }
        return null;
    }
}
