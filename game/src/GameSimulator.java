import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sam on 15-Oct-17.
 */
public class GameSimulator {

	public static void main(String[] args) {
		Map<String,Integer> gameResult;
		int games = 1;
		MossSideWhist game = new MossSideWhist(new GreedyAgent(), new RandomAgent(), new RandomAgent());
		game.playGame(games, System.out);
		gameResult =  game.getScores();
		int i = 0;
		for (String s:gameResult.keySet()) {
			System.out.printf("Agent: %s won %d tricks and %.5f percent\n",s,gameResult.get(s),(double)
			gameResult.get(s)/(double)(games*16*3)*100.0);
		}
	}
}
