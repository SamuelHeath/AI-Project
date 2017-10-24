import java.util.*;

/**
 * Created by Sam on 23-Oct-17.
 */
public class GameSimulatorGA {

	public MSWAgent testingAgent = null;

	public Map<String,Integer> gameResult;

	public GameSimulatorGA(MSWAgent my_agent) {
		testingAgent = my_agent;
	}

	public GameSimulatorGA() {}

	public int getWins() {
		return gameResult.get("Carlo Monty");
	}

	public void run() {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));
		int games = 1;
		MossSideWhist game = new MossSideWhist(testingAgent, new GreedyNaive(), new GreedyNaive());
		game.playGame(games, System.out);
		gameResult =  game.getScores();
		int i = 0;
		System.out.println(game.numDraw);
		for (String s:gameResult.keySet()) {
			System.out.printf("Agent: %s won %d \n", s, gameResult.get(s));
		}
		//for (Card c:deck)
		//System.out.println(c.toString());
	}
}
