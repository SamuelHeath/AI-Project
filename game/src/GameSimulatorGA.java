import java.util.*;

/**
 * Created by Sam on 23-Oct-17.
 */
public class GameSimulatorGA {
	public String name;
	public MSWAgent testingAgent = null;
	public Map<String,Integer> gameResult;

	public GameSimulatorGA(MSWAgent my_agent) {
		testingAgent = my_agent;
		this.name = generateName();
	}

	public GameSimulatorGA() {}

	public int getWins() {
		return gameResult.get("Carlo Monty");
	}

	public String generateName() {
		Random r = new Random();
		StringBuilder name = new StringBuilder("");
		char[] alpha = new char[] {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',
				'q'};
		for (int j = 0; j < 20; j++) {
			name.append(alpha[r.nextInt(alpha.length)]);
		}
		return name.toString();
	}

	public void run() {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));

		int games = 3;
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
