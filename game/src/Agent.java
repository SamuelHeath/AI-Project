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

	public Agent(double exploration, int depth) {
		explore = exploration;
		dep = depth;
	}

	public Agent() {
		//explore = 4.3432901157198955;
		//explore = 3.884345602601573; //GA obtained
		explore = 1.0/Math.sqrt(2);
		dep = 6;
		//dep = 0;
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
		Collections.sort(deck); // Can't Remember why I did this
		for (Card c : deck) unSeen.add(c);
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
			//for(Card c : hand) { System.out.print(c); System.out.print(" ");} // DEBUG
			int count = 0;
			Card[] ditch = new Card[4];
			// Ditch four cards but keep the spades.
			int size = hand.size()-1;
			for (int i = 0; i < size && count < 4; i++) {
				if (!hand.get(i).suit.equals(Suit.SPADES)) {
					//System.out.println("Ditching..."); // DEBUG
					Card d = hand.get(i);
					//System.out.println(d); // DEBUG
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
		long playTime = 180; // give 200ms to explore and respond.
		long startTime = System.currentTimeMillis();
		Node curr_node = new Node(null,null, -1);
		State curr_state = new State(trick,0,this.unSeen,this.hand,dep,playerHasSuit); //0 represents THIS player

		/*System.out.println("Trick Size: " + trick.size());
		System.out.print("My Cards: ");
		for (Card c:hand) System.out.print(c.toString()+ " ");
		System.out.println();*/

		Random rand = new Random();
		int x = 0;
		while (System.currentTimeMillis()-startTime < playTime && x < 1000) {
			//Information Set Monte Carlo Tree Search updating curr_node as we go.
			State state = curr_state.clone(); // Copies the state
			state.determinise(playerHasSuit);// Initially determinise, as this AI doesnt know what others have.

			/*System.out.print("Available cards: ");
			for (Card c:state.availableActions()) System.out.print(c.toString() + " ");
			System.out.println();*/

			while (curr_node.unexploredActions(state.availableActions()).size() == 0 &&
					state.availableActions().size() != 0) {

				curr_node = curr_node.selectChild(state.availableActions());
				state.performAction(curr_node.action);
			}

			List<Card> actions_to_expand = curr_node.unexploredActions(state.availableActions());
			if (actions_to_expand.size() > 0) {
				//Apply a heuristic to select a better card
				Card action;
				List<Card> winningMoves = state.getWinningCards(actions_to_expand);
				/*System.out.print("Trick: ");
				for (Card c:this.trick) System.out.print(c.toString()+" ");
				System.out.print(" My Cards: ");
				for (Card c:hand) System.out.print(c.toString()+ " ");
				System.out.print(" Selecting Best Cards: ");
				for (Card c:winningMoves) System.out.print(c.toString()+" ");
				System.out.println();*/


				action = actions_to_expand.get(rand.nextInt(actions_to_expand.size())); //No winning cards so
				curr_node = curr_node.addChild(action, state.player);
				state.performAction(action);
			}

			//System.out.println("Play Off\n-------------");
			while (state.availableActions().size() > 0 && curr_state.canGoDeepa()) {
				//Apply heuristic here
				//List<Card> winning_moves = state.getWinningCards();
				//From the moves which will get us a win choose the lowest, if no cards allow us a win play best card
				/*if (winning_moves.size() != 0) state.performAction(winning_moves.get(rand.nextInt
						(winning_moves.size())));*/
				state.performAction(state.availableActions().get(rand.nextInt(state.availableActions().size()
				)));
			}
			//System.out.println("-------------");

			while (curr_node != null) {
				curr_node.updateNode(state);
				if (curr_node.parent != null) curr_node = curr_node.parent;
				else break; //Never let curr_node be set to null;
			}
			x++;
		}

		/*System.out.println("Num Nodes Fully Explored "+x);
		System.out.println("Time: "+(startTime-System.currentTimeMillis()));
		Collections.sort(curr_node.children,new NodeComparator());
		System.out.print("Children: ");
		for (Node n:curr_node.children) System.out.print(n.action.toString() + " ");
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

