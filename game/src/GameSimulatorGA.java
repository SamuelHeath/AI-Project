import java.util.*;

/**
 * @author Sam Heath (21725083)
 * @author Andre Wang (21714084)
 */
public class GameSimulatorGA {
	public int name;
	public MSWAgent testingAgent = null;
	public Map<String,Integer> gameResult;

	public GameSimulatorGA(MSWAgent my_agent, int id) {
		testingAgent = my_agent;
		this.name = id;
	}

	public GameSimulatorGA() {}

	public int getWins() {
		return gameResult.get("Carlo Monty");
	}

	public void run() {
		gameResult = new HashMap();
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));

		int games = 2;
		MossSideWhist game = new MossSideWhist(testingAgent, new AgentTwo(),
				new GreedyAgent());
		game.playGame(games, System.out);
		gameResult.putIfAbsent("Carlo Monty", 0);
		gameResult.put("Carlo Monty", game.getScores().get("Carlo Monty")+gameResult.get("Carlo Monty"));
		//for (Card c:deck)
		//System.out.println(c.toString());
	}
}
