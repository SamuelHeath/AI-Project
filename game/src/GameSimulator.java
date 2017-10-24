import java.util.*;

/**
 * Created by Sam on 15-Oct-17.
 */
public class GameSimulator {

	public static void main(String[] args) {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));
		Map<String,Integer> gameResult;
		int games = 20;
		MossSideWhist game = new MossSideWhist(new Agent(), new GreedyNaive(), new GreedyNaive());
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
