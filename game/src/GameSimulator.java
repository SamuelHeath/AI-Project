import java.util.*;

/**
 * Created by Sam on 15-Oct-17.
 */
public class GameSimulator {

	public static void main(String[] args) {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));
		Map<String,Integer> gameResult;

		int games = 5;
		//MossSideWhist game = new MossSideWhist(new Agent(), new GreedyAgent(), new GreedyAgent());
        //System.out.println("simNumber, agent, final_score, number_rounds");
        MSWAgent a = new AgentTwo();
        MSWAgent b = new GreedyAgent();
        MSWAgent c = new GreedyAgent();

        playGameManyTimes(150, games, a,b,c);
	}

	private static void playGame(int nGames, MSWAgent a, MSWAgent b, MSWAgent c) {
	    Map<String, Integer> gameResult;
    	MossSideWhist game = new MossSideWhist(a,b,c);
		game.playGame(nGames, System.out);
		gameResult =  game.getScores();
		//System.out.println(game.numDraw);
		for (String s:gameResult.keySet()) {
			//System.out.printf("Agent: %s won %d \n", s, gameResult.get(s));
            //System.out.printf("%s, %d, %d\n", s, gameResult.get(s), nGames);
		}
		//System.out.println("--------");
        System.out.println(getWinner(gameResult));
    }

    private static void playGameManyTimes(int numberOfSimulations, int nGames,
                                          MSWAgent a, MSWAgent b, MSWAgent c) {
	    for (int i = 0; i < numberOfSimulations; i++) {
	        playGame(nGames, a, b, c);
        }
    }

    private static String getWinner(Map<String, Integer> scores) {
	    int max = Collections.max(scores.values());
	    for (String s : scores.keySet()) {
	        if (scores.get(s) == max) return s; // doesn't consider draws.
        }
        return null;
    }
}
