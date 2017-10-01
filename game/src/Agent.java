import java.util.*;

/**
 * Created by Sam on 23-Sep-17.
 */
public class Agent implements MSWAgent {

	private String name = "Carlo Monty";
	private boolean lead;
	List<Card> seen;
	List<Card> unSeen;
	public static Map<String,Integer> AGENTMAP;
	public static Map<Suit,Integer> SUITMAP;
	//Boolean[i] is each player Boolean[i][j] is if we assume they have that suit in the order C,D,H,S
	Boolean[][] playerHasSuit;
	List<Card> hand;
	List<Card> trick;

	public void setup(String agentLeft, String agentRight) {
		AGENTMAP = new TreeMap();
		AGENTMAP.put(agentLeft,1);
		AGENTMAP.put(agentRight,2);
		SUITMAP = new TreeMap();
		SUITMAP.put(Suit.CLUBS,0);
		SUITMAP.put(Suit.DIAMONDS,1);
		SUITMAP.put(Suit.HEARTS,2);
		SUITMAP.put(Suit.SPADES,3);
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck);
		for (Card c : deck) unSeen.add(c);
		hand = new LinkedList();
		trick = new LinkedList();
		playerHasSuit = new Boolean[][] {{true, true, true, true},{true, true, true, true},{true, true, true, true}};
	}

	public void seeHand(List<Card> hand, int order) {
		for (Card c : hand) {
			seen.add(c);
			unSeen.remove(c);
			hand.add(c);
		}
		if (order == 0) {
			lead = true;
		}
	}

	public Card[] discard() {
		if (!lead) {
			return new Card[] {};
		} else { //Ditch the lowest rank non-trump cards.
			CardComparator cardComparator = new CardComparator(true);
			Collections.sort(hand,cardComparator);
			int count = 0;
			Card[] ditch = new Card[4];
			for (int i = 0; i < hand.size() && count < 4; i++) {
				if (!hand.get(i).suit.equals(Suit.SPADES)) {
					ditch[count++] = hand.remove(i);
				}
			}
			return ditch;
		}
	}

	public Card playCard() {
		long playTime = 190; // give 190ms to explore and respond.
		Node root_node = new Node(null,null);
		State curr_state = new State(trick,this.name,unSeen,hand);
		Random rand = new Random();
		while (System.currentTimeMillis() < playTime) {
			//Information Set Monte Carlo Tree Search updating root_node as we go.
			Node curr_node = root_node;
			curr_state.determinise(playerHasSuit); // Initially determinise, as this AI doesnt know what others
			// have.

			while (curr_node.unexploredActions(curr_state.availableActions()).size() == 0 &&
					curr_state.availableActions().size() != 0) {

				curr_node = curr_node.selectChild(curr_state.availableActions());
				curr_state.performAction(curr_node.action);
			}

			List<Card> actions_to_expand = curr_node.unexploredActions(curr_state.availableActions());
			if (actions_to_expand.size() > 0) {
				//Apply a heuristic to select a better card
				Card action = actions_to_expand.get(rand.nextInt(actions_to_expand.size()));
				curr_node = curr_node.addChild(action);
				curr_state.performAction(action);
			}

			while (curr_state.availableActions().size() > 0 && curr_state.getNumTricks() <= curr_state.max_tricks) {
				//Apply heuristic here
				curr_state.performAction(curr_state.availableActions().get(rand.nextInt()));
			}

			while (curr_node != null) {
				curr_node.updateNode(curr_state);
				curr_node = curr_node.parent;
			}
		}
		return Collections.max(root_node.children, new NodeComparator()).action;
	}

	public void seeCard(Card card, String agent) {
		trick.add(card);
		if (trick.size() > 1 && trick.get(0).suit != trick.get(trick.size()-1).suit) {
			//We now have information about who holds the remaining cards of that suit.
			playerHasSuit[AGENTMAP.get(agent)][SUITMAP.get(trick.get(0).suit)] = false;
		} else {
		}
	}

	public void seeResult(String winner) {
		trick.clear();
		if (winner.equals(this.name)) {
			//set 1
		} else {
			//set 0
		}
	}

	public void seeScore(Map<String,Integer> scores) {

	}

	public String sayName() {
		return this.name;
	}
}

/**
 * Used to calculate the best child node of the root based on num visits.
 */
class NodeComparator implements Comparator<Node> {

	@Override
	public int compare(Node a, Node b) {
		return a.visits < b.visits ? 1 : a.visits == b.visits ? 0 : -1;
	}
}

class CardComparator implements Comparator<Card> {
	boolean sortBy;

	public CardComparator(boolean sortByRank) {
		sortBy = sortByRank;
	}

	@Override
	public int compare(Card a, Card b) {
		if (sortBy) return a.rank < b.rank ? -1 : a.rank == b.rank ? 0 : 1;
		else return a.toString().compareTo(b.toString());
	}
}