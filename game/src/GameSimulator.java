import java.util.*;

/**
 * Created by Sam on 15-Oct-17.
 */
public class GameSimulator {

	public static void main(String[] args) {
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck, new CardComparator(true));
		Map<String,Integer> gameResult;
		int games = 1;
		MossSideWhist game = new MossSideWhist(new Agent(), new GreedyAgent(), new GreedyAgent());
		game.playGame(games, System.out);
		gameResult =  game.getScores();
		int i = 0;
		System.out.println(game.numDraw);
		for (String s:gameResult.keySet()) {
			System.out.printf("Agent: %s won %d rounds and %.5f percent\n",s,gameResult.get(s),(double)
			gameResult.get(s)/(double)((games*3))*100.0);
		}
		//for (Card c:deck)
			//System.out.println(c.toString());
	}
}
