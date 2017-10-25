import java.util.*;

/**
 * Created by Sam on 15-Oct-17.
 */
public class GameSimulator {

	public static void main(String[] args) {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));
		Map<String,Integer> gameResult;

		int games = 3;
		//MossSideWhist game = new MossSideWhist(new Agent(), new GreedyAgent(), new GreedyAgent());
        //System.out.println("simNumber, agent, final_score, number_rounds");
        MSWAgent a = new AgentTwo();
        MSWAgent b = new RandomAgent();
        MSWAgent c = new RandomAgent();

		System.out.println(a.sayName() + ",\t" + b.sayName() + ",\t" + c.sayName());
        for (int i = 0; i < 100; i++) {
        	playGameManyTimes(10, games, a,b,c);
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
                                          MSWAgent a, MSWAgent b, MSWAgent c) {
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
					wins.get(c.sayName()));
    }

    private static String getWinner(Map<String, Integer> scores) {
	    int max = Collections.max(scores.values());
	    for (String s : scores.keySet()) {
	        if (scores.get(s) == max) return s; // doesn't consider draws.
        }
        return null;
    }
}
