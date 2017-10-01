import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Sam on 01-Oct-17.
 */
public class RandomAgent implements MSWAgent {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;

	private String agent_name = "Random Agent";
	public List<Card> hand;
	public List<Card> trick;
	private Random random = new Random();

	/**
	 * Tells the agent the names of the competing agents, and their relative position.
	 * */
	public void setup(String agentLeft, String agentRight) {
		hand = new LinkedList();
		trick = new LinkedList();
	}

	/**
	 * Starts the round with a deal of the cards.
	 * The agent is told the cards they have (16 cards, or 20 if they are the leader)
	 * and the order they are playing (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
	 * */
	public void seeHand(List<Card> hand, int order) {
		this.hand = hand;
	}

	/**
	 * This method will be called on the leader agent, after the deal.
	 * If the agent is not the leader, it is sufficient to return an empty array.
	 */
	public Card[] discard() {
		Card[] ditch = new Card[4];
		for (int i = 0; i < 4; i++) {
			ditch[i] = hand.remove(random.nextInt(hand.size()));
		}
		return ditch;
	}

	public List<Card> getAvailableCards(Suit lead_suit) {
		List<Card> moves = new LinkedList();
		boolean hasLeadSuit = false;
		for (Card c : this.hand) {
			if (c.suit == lead_suit) {
				hasLeadSuit = true;
				moves.add(c);
			} else if (c.suit == Suit.SPADES) moves.add(c);
		}
		if (!hasLeadSuit) {
			moves.clear();
			moves.addAll(this.hand);
		}
		return moves;
	}

	/**
	 * Agent returns the card they wish to play.
	 * A 200 ms timelimit is given for this method
	 * @return the Card they wish to play.
	 * */
	public Card playCard() {
		Card bestOption;
		if (trick.size() > 0) {
			List<Card> moves = getAvailableCards(trick.get(0).suit);
			bestOption = moves.get(random.nextInt(moves.size()));
			this.hand.remove(bestOption);
		} else {
			bestOption = this.hand.remove(random.nextInt(this.hand.size()));
		}
		trick.add(bestOption);
		return bestOption;
	}
	/**
	 * Sees an Agent play a card.
	 * A 50 ms timelimit is given to this function.
	 * @param card, the Card played.
	 * @param agent, the name of the agent who played the card.
	 * */
	public void seeCard(Card card, String agent) {
		if (!agent.equals(this.agent_name)) {

		}
	}

	/**
	 * See the result of the trick.
	 * A 50 ms timelimit is given to this method.
	 * This method will be called on each eagent at the end of each trick.
	 * @param winner, the player who played the winning card.
	 * */
	public void seeResult(String winner) {
		trick.clear();
	}

	/**
	 * See the score for each player.
	 * A 50 ms timelimit is givien to this method
	 * @param scoreboard, a Map from agent names to their score.
	 **/
	public void seeScore(Map<String, Integer> scoreboard) {

	}

	/**
	 * Returns the Agents name.
	 * A 10ms timelimit is given here.
	 * This method will only be called once.
	 * */
	public String sayName() { return this.agent_name; }

}
