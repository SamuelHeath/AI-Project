import sun.management.resources.agent;

import java.util.*;

/**
 * Created by Sam on 23-Sep-17.
 */
public class Agent implements MSWAgent {

	public static double explore;
	private int dep; //number of tricks to look ahead by

	private int[] num_wins;
	private String name = "Carlo Monty";
	public static boolean lead;
	private List<Card> seen = new ArrayList<>(52);
	private List<Card> unSeen = new ArrayList<>(52);
	public static Map<String,Integer> AGENTMAP;
	public static Map<Suit,Integer> SUITMAP;
	//Boolean[i] is each player Boolean[i][j] is if we assume they have that suit in the order C,D,H,S
	boolean[][] playerHasSuit;
	List<Card> hand;
	List<Card> trick;
	int trick_count = 1;

	int[] trickToIteration = new int[16];

	public Agent(double exploration, int depth) {
		explore = exploration;
		dep = depth;
	}

	public Agent(String name) {
		this.name = name;
		explore = 1.683864981087254;
		//explore = 3.884345602601573; //GA obtained
		//explore = 1.0/Math.sqrt(2);
		dep = 9;
	}

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
		ArrayList<Card> deck = new ArrayList<>(Arrays.asList(Card.values()));
		Collections.sort(deck,new CardComparator(true)); // Can't Remember why I did this
		for (Card c : deck) {
			unSeen.add(c);
		}
		for (int i = 0; i < 16; i++) trickToIteration[i] = 0;
		hand = new LinkedList();
		trick = new LinkedList();
		playerHasSuit = new boolean[][] {{true, true, true, true},{true, true, true, true},{true, true, true, true}};
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

    /**
     * This method will be called on the leader agent, after the deal.
     * If the agent is not the leader, it is sufficient to return an empty array.
     */
	public Card[] discard() {
		if (!lead) {
			return new Card[] {};
		} else { //Ditch the lowest rank non-trump cards.
			CardComparator cardComparator = new CardComparator(true);
			Collections.sort(hand, cardComparator);
			int count = 0;
			Card[] ditch = new Card[4];
			// Ditch four cards but keep the spades.
			int size = hand.size()-1;
			for (int i = 0; i < size && count < 4; i++) {
				if (!hand.get(i).suit.equals(Suit.SPADES)) {
					Card d = hand.get(i);
					ditch[count++] = d; // do not mutate the hand yet
				}
			}
			for (Card c:ditch) hand.remove(c);
			// update our hand
			return ditch;
		}
	}


    /**
     * Agent returns the card they wish to play.
     * A 200 ms timelimit is given for this method
     * @return the Card they wish to play.
     */
	public Card playCard() {
		long playTime = 190; // give 200ms to explore and respond.
		long startTime = System.currentTimeMillis();
		Node curr_node = new Node(null,null, -1);
		State curr_state = new State(this.trick,0,this.unSeen,this.hand,dep,playerHasSuit); //0
		// represents THIS player

		/*if (trick.size() > 0) {
			System.out.print("Trick: ");
			for (Card c:trick) System.out.print(c.toString()+" ");
			System.out.println();
		}*/

		Random rand = new Random();
		int x = 0;
		//System.out.println("Expansion: "+(int)(177.79*Math.exp(0.2995*trick_count)));
		while (System.currentTimeMillis()-startTime < playTime) {
			//Information Set Monte Carlo Tree Search updating curr_node and state.
			State state = curr_state.clone(); // Copies the state
			state.determinise(playerHasSuit);// Initially determinise, as this AI doesn't know what others have.

			/*System.out.print("Determinisation: ");
			for (int i = 0; i < 2; i++) {
				for (Card c: state.player_hands[i+1]) {
					System.out.print(c.toString() + " ");
				}
				System.out.print(" | ");
			}
			System.out.println();

			List<Card> wins = state.getWinningCards();
			List<Card> moves = state.availableActions();
			System.out.print("Moves Available: ");
			for (Card c:moves) {
				System.out.print(c.toString()+" ");
			}
			if (state.trick.size()>0) {
				System.out.print("Has suit: "+state.playerHasSuit(0,state.trick.get(0).suit));
			}
			System.out.print("\nWinning Moves: ");
			for (Card c:wins) {
				System.out.print(c.toString()+" ");
			}
			System.out.println();*/

			//Select Stage
			while (curr_node.unexploredActions(state.availableActions()).size() == 0 &&
					state.availableActions().size() != 0) {
				curr_node = curr_node.selectChild(state.availableActions());
				state.performAction(curr_node.action);
			}

			//Expand Stage
			List<Card> actions_to_expand = curr_node.unexploredActions(state.availableActions());
			if (actions_to_expand.size() > 0) {
				//Apply a heuristic to select a better card
				Card action;

				List<Card> winningMoves = state.getWinningCards(actions_to_expand); //Expand nodes we can win
				if (winningMoves.size() > 0) {
					Collections.sort(winningMoves,new CardComparator(true));
					action = winningMoves.get(rand.nextInt(winningMoves.size()));
				} else action = actions_to_expand.get(rand.nextInt(actions_to_expand.size())); //No winning
				// cards
				curr_node = curr_node.addChild(action, state.player);
				state.performAction(action);
				//System.out.println("Expansion: "+action);
			}

			//Play-Off Stage
			while (state.availableActions().size() > 0 && curr_state.canGoDeepa()) {
				//Apply heuristic here
				List<Card> winning_moves = state.getWinningCards();
				//Sort by lowest spade
				Collections.sort(winning_moves,new CardComparator(false,true));
				//From the moves which will get us a win choose the lowest, if no cards allow us a win
				//play best card
				if (winning_moves.size() != 0) {
					state.performAction(winning_moves.get(rand.nextInt(winning_moves.size())));
				} else {
					state.performAction(state.availableActions().get(rand.nextInt(state.availableActions().size())));
				}
			}

			//Backtrack Stage
			while (curr_node != null) {
				curr_node.updateNode(state);
				if (curr_node.parent != null) curr_node = curr_node.parent;
				else break; //Never let curr_node be set to null;
			}
			x++;
		}

		//System.out.println(x);
		trickToIteration[trick_count%16] += x;
		//System.out.println("Time: "+(System.currentTimeMillis()-startTime));
		Collections.sort(curr_node.children,new NodeComparator());
		/*System.out.print("Children: ");
		for (Node n:curr_node.children) System.out.print("Action: " + n.action+ " Value "+ n.ISUCT() +
				" ");
		System.out.println();*/
		Card c = curr_node.children.get(curr_node.children.size()-1).action;
		//System.out.println("Playing: " + c.toString());
		return c;
	}


    /**
     * Sees an Agent play a card.
     * A 50 ms timelimit is given to this function.
     * @param card, the Card played.
     * @param agent, the name of the agent who played the card.
     */
	public void seeCard(Card card, String agent) {
		trick.add(card);
		if (agent.equals(this.name)) this.hand.remove(card);
		if (agent != this.name)
			unSeen.remove(card);
		if (trick.size() > 1 && trick.get(0).suit != trick.get(trick.size()-1).suit) {
			//We now have information about who holds the remaining cards of that suit.
			playerHasSuit[AGENTMAP.get(agent)][SUITMAP.get(trick.get(0).suit)] = false;
		}
	}


    /**
     * See the result of the trick.
     * A 50 ms timelimit is given to this method.
     * This method will be called on each eagent at the end of each trick.
     * @param winner, the player who played the winning card.
     */
	public void seeResult(String winner) {
		trick.clear();
		trick_count++;
		if (winner.equals(this.name)) {

		} else {
			//set 0
		}
	}


    /**
     * See the score for each player.
     * A 50 ms timelimit is givien to this method
     * @param scoreboard, a Map from agent names to their score.
     */
	public void seeScore(Map<String,Integer> scoreboard) {
		unSeen.clear();
		hand.clear();
		playerHasSuit = new boolean[][] {{true, true, true, true},{true, true, true, true},{true, true, true, true}};
		lead = false;
		trick_count = 0;
		unSeen = new LinkedList(Arrays.asList(Card.values()));
	}

    /**
     * Returns the Agents name.
     * A 10ms timelimit is given here.
     * This method will only be called once.
     */
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
        // more efficient
        return a.num_visits - b.num_visits;
		//return a.num_visits < b.num_visits ? 1 : a.num_visits == b.num_visits ? 0 : -1;
	}
}

