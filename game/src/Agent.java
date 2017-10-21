import java.util.*;

/**
 * Created by Sam on 23-Sep-17.
 */
public class Agent implements MSWAgent {

	int[] num_wins;
	private String name = "Carlo Monty";
	private boolean lead;
	List<Card> seen = new LinkedList();
	List<Card> unSeen = new LinkedList();
	public static Map<String,Integer> AGENTMAP;
	public static Map<Suit,Integer> SUITMAP;
	//Boolean[i] is each player Boolean[i][j] is if we assume they have that suit in the order C,D,H,S
	Boolean[][] playerHasSuit;
	List<Card> hand;
	List<Card> trick;

	public void setup(String agentLeft, String agentRight) {
		AGENTMAP = new HashMap();
		AGENTMAP.put(this.name,0);
		AGENTMAP.put(agentLeft,1);
		AGENTMAP.put(agentRight,2);
		SUITMAP = new HashMap();
		SUITMAP.put(Suit.CLUBS,0);
		SUITMAP.put(Suit.DIAMONDS,1);
		SUITMAP.put(Suit.HEARTS,2);
		SUITMAP.put(Suit.SPADES,3);
		num_wins = new int[] {0,0,0};
		ArrayList<Card> deck = new ArrayList(Arrays.asList(Card.values()));
		Collections.sort(deck); // Can't Remember why I did this
		for (Card c : deck) unSeen.add(c);
		hand = new LinkedList();
		trick = new LinkedList();
		playerHasSuit = new Boolean[][] {{true, true, true, true},{true, true, true, true},{true, true, true, true}};
	}

	public void seeHand(List<Card> hand, int order) {
		this.hand = hand;
		for (Card c : hand) {
			seen.add(c);
			unSeen.remove(c);
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
		long playTime = 200; // give 200ms to explore and respond.
		long startTime = System.currentTimeMillis();
		Node root_node = new Node(null,null, -1);
		State curr_state = new State(trick,0,unSeen,hand); //0 represents THIS player
		curr_state.determinise(playerHasSuit);// Initially determinise, as this AI doesnt know what
		// others have.

		Random rand = new Random();
		int x = 0;
		//while (System.currentTimeMillis()-startTime < playTime) {
			//Information Set Monte Carlo Tree Search updating root_node as we go.
			Node curr_node = root_node;
			State state = curr_state;
			for (int i = 0; i < 3; i++) {
				System.out.print("Player " + i + ": ");
				for (Card c:state.player_hands[i])
					System.out.print(c.toString() + " ");
				System.out.println();
			}
			System.out.println(state.availableActions());
			while (curr_node.unexploredActions(state.availableActions()).size() == 0 &&
					state.availableActions().size() != 0) {

				curr_node = curr_node.selectChild(state.availableActions());
				System.out.println("Tree: " + curr_node.action);
				state.performAction(curr_node.action);
			}
			List<Card> actions_to_expand = curr_node.unexploredActions(state.availableActions());
			if (actions_to_expand.size() > 0) {
				//Apply a heuristic to select a better card
				Collections.sort(actions_to_expand, new CardComparator(true));
				Card action = actions_to_expand.get(0);
				System.out.println("Expand: " + action);
				curr_node = curr_node.addChild(action,state.player);
				state.performAction(action);
			}

			//&& curr_state.getNumTricks() <= curr_state.max_tricks
			while (state.availableActions().size() > 0 ) {
				//Apply heuristic here
				state.performAction(state.availableActions().get(rand.nextInt(state
						.availableActions().size())));
			}

			while (curr_node != null) {
				curr_node.updateNode(state);
				curr_node = curr_node.parent;
			}
			x++;
		//}
		System.out.println(x);
		System.out.println(System.currentTimeMillis()-startTime);
		Collections.sort(root_node.children,new NodeComparator());
		System.out.println("Playing: " + root_node.children.get(0).action);
		hand.remove(root_node.children.get(0).action);
		return root_node.children.get(0).action;
	}

	public void seeCard(Card card, String agent) {
		trick.add(card);
		if (agent != this.name)
			unSeen.remove(card);
		if (trick.size() > 1 && trick.get(0).suit != trick.get(trick.size()-1).suit) {
			//We now have information about who holds the remaining cards of that suit.
			playerHasSuit[AGENTMAP.get(agent)][SUITMAP.get(trick.get(0).suit)] = false;
		} else {
		}
	}

	public void seeResult(String winner) {
		trick.clear();
		if (winner.equals(this.name)) {

		} else {
			//set 0
		}
	}

	public void seeScore(Map<String,Integer> scores) {
		unSeen.clear();
		hand.clear();
		playerHasSuit = new Boolean[][] {{true, true, true, true},{true, true, true, true},{true, true, true, true}};
		lead = false;
		unSeen = new LinkedList(Arrays.asList(Card.values()));
	}

	public String sayName() {
		return this.name;
	}
}

/**
 * Used to calculate the best child node of the root based on num num_visits.
 */
class NodeComparator implements Comparator<Node> {

	@Override
	public int compare(Node a, Node b) {
		return a.num_visits < b.num_visits ? 1 : a.num_visits == b.num_visits ? 0 : -1;
	}
}

