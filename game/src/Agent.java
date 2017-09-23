import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 23-Sep-17.
 */
public class Agent implements MSWAgent {

	private String name = "Carlo Monty";

	public void setup(String agentLeft, String agentRight) {

	}

	public void seeHand(List<Card> hand, int order) {

	}

	public Card[] discard() {
		return null;
	}

	public Card playCard() {
		return null;
	}

	public void seeCard(Card card, String agent) {

	}

	public void seeResult(String winner) {

	}

	public void seeScore(Map<String,Integer> scores) {

	}

	public String sayName() {
		return this.name;
	}
}
