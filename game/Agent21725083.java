import java.util.*;

/**
 * SO-ISMCTS
 * @author Sam Heath (21725083)
 * @author Andre Wang (21714084)
 */
public class Agent21725083 implements MSWAgent {

/* Genetic Algorithm Values
	explore = 1.683864981087254;
	//explore = 3.884345602601573; //GA obtained
	//explore = 1.0/Math.sqrt(2);
	depth = 9;
*/

	public static double explore = 1.683864981087254;
	private int depth = 9; //number of tricks to look ahead by

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
			// update our hand
			for (Card c:ditch) hand.remove(c);
			return ditch;
		}
	}


    /**
     * Agent returns the card they wish to play.
     * A 200 ms timelimit is given for this method
     * @return the Card they wish to play.
     */
	public Card playCard() {
		long playTime = 185; // give 185ms to explore and respond.
		long startTime = System.currentTimeMillis();
		Node curr_node = new Node(null,null, -1);
		State curr_state = new State(this.trick,0,this.unSeen,this.hand, depth,playerHasSuit); //0
		// represents THIS player

		Random rand = new Random();
		int x = 0;
		//System.out.println("Expansion: "+(int)(223*Math.exp(0.3005*trick_count)));
		while (System.currentTimeMillis()-startTime < playTime) {
			//Information Set Monte Carlo Tree Search updating curr_node and state.
			State state = curr_state.clone(); // Copies the state
			state.determinise(playerHasSuit);// Initially determinise, as this AI doesn't know what others have.

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
				} else action = actions_to_expand.get(rand.nextInt(actions_to_expand.size())); //No winning cards
				curr_node = curr_node.addChild(action, state.player);
				state.performAction(action);
			}

			//Play-Off Stage
			while (state.availableActions().size() > 0 && curr_state.canGoDeepa()) {
				//Apply heuristic here
				List<Card> winning_moves = state.getWinningCards();
				//Sort by lowest spade
				Collections.sort(winning_moves,new CardComparator(false,true));
				//From the moves which will get us a win choose the lowest, if no cards allow us a win play best card
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
		Collections.sort(curr_node.children,new NodeComparator());
		if (curr_node.children.size() > 0) {
			Card c = curr_node.children.get(curr_node.children.size() - 1).action;
			return c;
		} else {
			return this.hand.get(rand.nextInt(this.hand.size())); //Random if we somehow get an error
		}
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
	}
}

class State {

	int[] num_wins;
	int num_moves; // Number of moves of this trick
	int player;
	int playerNext;
	List<Card> trick;
	List<Card>[] player_hands = new List[3];
	Random r = new Random();
	int num_tricks;
	int max_tricks;
	List<Card> unseen;
	boolean[][] player_has_suit;

	public State(List<Card> trick, int cur_player, List<Card> unseen, List<Card> my_hand, int max_depth,boolean[][] suits) {
		this.trick = new LinkedList(trick);
		this.player_has_suit = suits.clone();
		num_moves = trick.size();
		player = cur_player;
		playerNext = (this.player+1)%3;
		this.unseen = new LinkedList(unseen);
		player_hands[0] = new LinkedList(my_hand);
		player_hands[1] = new LinkedList();
		player_hands[2] = new LinkedList();
		num_tricks = 0;
		max_tricks = max_depth; // 18 Moves ahead
		num_wins = new int[] {0,0,0};
	}

	/**
	 * Determinise the root game state by taking the Imperfect Game Info and convert it into a Perfect
	 * Info game.
	 */
	public void determinise(boolean[][] suitAvail) {
		List<Card> cards = this.unseen;
		//Build-up decks based on information we have gained!
		List<Card> cardsForBoth = new LinkedList(); //Cards either player could have based on our info

		int size = cards.size();
		if (!Agent21725083.lead && size <= 36 && size >= 32) { // You aren't the leader and u may not have
			// started the game
			Collections.sort(cards, new CardComparator(true));
			for (int i = 0; i < 4; i++) {
				Card c = cards.remove(0);
				this.unseen.remove(c);
			}
			size = cards.size(); //We havent seen which cards were thrown away but we can assume
		}

		//Randomly assign the remaining cards
		Random rand = new Random();
		Collections.shuffle(this.unseen);

		for (int i = 0; i < size; i++) {
			if (suitAvail[1][Agent21725083.SUITMAP.get(cards.get(i).suit)] && suitAvail[2][Agent21725083.SUITMAP.get(cards.get(i).suit)]) {
				cardsForBoth.add(cards.get(i));
			} else if (suitAvail[1][Agent21725083.SUITMAP.get(cards.get(i).suit)] && !suitAvail[2][Agent21725083.SUITMAP.get(cards.get(i).suit)]) {
				player_hands[1].add(cards.get(i));
			} else if (!suitAvail[1][Agent21725083.SUITMAP.get(cards.get(i).suit)] && suitAvail[2][Agent21725083.SUITMAP.get(cards.get(i).suit)]) {
				player_hands[2].add(cards.get(i));
			}
		}

		if (cardsForBoth.size() != 0) {
			int k = rand.nextInt(cardsForBoth.size());
			player_hands[1].add(cardsForBoth.remove(k));
		}

		size = cardsForBoth.size();
		for (int i = 0; i < size; i++) {
			int k = rand.nextInt(cardsForBoth.size());
			if (player_hands[1].size() == player_hands[2].size()) {
				player_hands[1].add(cardsForBoth.remove(k));
			} else if (player_hands[1].size() > player_hands[2].size()) {
				player_hands[2].add(cardsForBoth.remove(k));
			} else {
				player_hands[1].add(cardsForBoth.remove(k));
			}
		}
	}

	/**
	 * For the current player at this state point what moves can they do?
	 */
	public List<Card> availableActions() {
		List<Card> hand = player_hands[player];
		if (this.trick.size() == 0) {
			return hand;
		} else {
			Card first = this.trick.get(0);
			List<Card> playable = new LinkedList();
			for (Card c : hand) {
				if (c.suit == first.suit) playable.add(c);
			}
			if (playable.size() == 0) return hand;
			return playable;
		}
	}

	/**
	 * Can we go deeper?
	 */
	public boolean canGoDeepa() { return this.num_tricks <= this.max_tricks; }

	private void printScores() {
		for (int i = 0; i < num_wins.length; i++) {
			System.out.print("Player " + i + ": Won " + num_wins[i] + " Cards: ");
			for (Card c:player_hands[i]) System.out.print(c.toString() + " ");
			System.out.println();
		}
	}

	/**
	 * From this current state point the player performs an action (plays a card)
	 * @param action
	 */
	public void performAction(Card action) {
		this.player_hands[this.player].remove(action);
		this.unseen.remove(action);
		//System.out.println("Player " + player + ": " + action.toString());
		this.trick.add(action);
		if (this.trick.size() == 3) {
			//Calc who num_wins and so who starts the round.
			int winner = calcWinner();
			num_wins[winner]++;
			this.player = winner;
			this.playerNext = (winner+1)%3;
			//System.out.println("Player " + winner + ": WINS");
			//printScores();
			this.trick.clear();
			//System.out.println();
			this.num_tricks++; //Update current depth.
		} else {
			setPlayerNext();
		}
	}

	private Card getBestCardFromSuit(int player_index, Suit searchSuit) {
		Card bestCard = null;
		for (Card c: player_hands[player_index]) {
			if (bestCard == null && c.suit == searchSuit) bestCard = c;
			else if (bestCard != null && c.suit == searchSuit && bestCard.rank < c.rank) bestCard = c;
		}
		return bestCard;
	}

	public boolean playerHasSuit(int player_index, Suit searchSuit) {
		for (Card c:player_hands[player_index]) if (c.suit==searchSuit) return true;
		return false;
	}

	public List<Card> getWinningCards() {
		return getWinningCards(this.availableActions());
	}

	public List<Card> getWinningCards(List<Card> availableMoves) {
		List<Card> bestMoves = new LinkedList();
		if (availableMoves.size() == 0) return bestMoves;
		int player_next1 = (player+1)%3;
		int player_next2 = (player+2)%3;
		if (trick.size() == 0) {
			Card spade = null;
			for (Card c:availableMoves) {
				//if the player is missing a suit and doesnt have spades then play that card
				if (!playerHasSuit(player_next1, c.suit) && !playerHasSuit(player_next2, c.suit) &&
						!playerHasSuit(player_next1,Suit.SPADES) && !playerHasSuit(player_next2,Suit.SPADES)) {
					bestMoves.add(c);
					//If they have your suit but couldnt beat your card add it
				} else if (playerHasSuit(player_next1,c.suit) && playerHasSuit(player_next2,c.suit)) {
					if (getBestCardFromSuit(player_next1, c.suit) != null && getBestCardFromSuit
							(player_next1, c.suit).rank < c.rank && getBestCardFromSuit(player_next2, c.suit) !=
							null && getBestCardFromSuit(player_next2, c.suit).rank < c.rank) {
						bestMoves.add(c);
					}
				} else { //Play the best card or lowest spade to draw out others spades
					if (c.suit == Suit.SPADES) {
						if (spade == null) spade = c;
						if (spade != null && spade.rank > c.rank) {
							spade = c; //Play lowest Spade to draw out the other plays spades and hopefully do
							// above move.
						}
					}
				}
			}
			if (!bestMoves.contains(spade) && spade!=null) {
				bestMoves.add(spade); // Lowest spade
			}
			return bestMoves;
		}
		Card toBeat = trick.get(0);
		Card orig = trick.get(0);
		Collections.sort(availableMoves);
		if (trick.size() == 1) {
			if (playerHasSuit(player,orig.suit)) { //original suit
				Card worst = availableMoves.get(0); //get worst.
				for (Card c:availableMoves) {
					if (c.suit == orig.suit) { //If we can play this
						if (c.rank > orig.rank) { //if we can beat the original card with this one
							if (playerHasSuit(playerNext, orig.suit)) { //can we beat the next player
								if (getBestCardFromSuit(player_next1, orig.suit) != null && getBestCardFromSuit(playerNext, orig.suit)
										.rank < c.rank) {
									bestMoves.add(c);
								} else {
									if (worst.rank > c.rank && c.suit != Suit.SPADES) worst = c;
								}
								//If the next player doesnt have the correct suit
							} else {
								//Wrong suit but they can play spades
								if (playerHasSuit(playerNext, Suit.SPADES)) {
									if (worst.rank > c.rank && c.suit != Suit.SPADES) worst = c; //Here we lose worst
								} else {
									if (worst.rank > c.rank && c.suit != Suit.SPADES)
										worst = c; //Here we win with worst
								}
							}
						}
					}
				}
				if (bestMoves.size() == 0) bestMoves.add(worst);
				return bestMoves;
			} else {
				if (!playerHasSuit(playerNext, Suit.SPADES)) {
					Card bestNextCard = getBestCardFromSuit(player, Suit.SPADES);
					if (bestNextCard != null) bestMoves.add(bestNextCard);
					return bestMoves; //Could be no best moves
				}
				bestMoves.add(availableMoves.get(0));
			}
		} else if (trick.size() == 2) { // 2 cards Played ur last player
			Card c1 = trick.get(1);
			Card worst = availableMoves.get(0);
			if (c1.suit != toBeat.suit) {
				if (c1.suit == Suit.SPADES) { //c1 will beat first card
					toBeat = c1;
				} //original is still the best card
			} else {
				if (c1.rank > orig.rank) toBeat = c1; //c1 is same suit and better rank so would win.
			}

			if (playerHasSuit(player, orig.suit)) {
				if (orig.suit == c1.suit) {
					for (Card c : availableMoves) {
						if (c.suit == orig.suit && c.rank > toBeat.rank) {
							bestMoves.add(c);
						}
					}
					if (bestMoves.size() == 0) {
						bestMoves.add(worst);
					}
					return bestMoves;
				} else if (c1.suit == Suit.SPADES) { //We have the correct suit but we're going to lose
					bestMoves.add(worst);
					return bestMoves;
				}
			} else {
				if (playerHasSuit(player, Suit.SPADES)) {
					//If we are able to beat the last player then play worst card that wins
					if (toBeat.suit == Suit.SPADES && getBestCardFromSuit(player,Suit.SPADES) != null &&
							getBestCardFromSuit(player,Suit.SPADES).rank > toBeat.rank) {
						for (Card c : availableMoves) { //For all cards find all that can win
							if (c.suit == Suit.SPADES && c.rank > toBeat.rank) {
								bestMoves.add(c);
							}
						}
						return bestMoves;
					}
				} else {
					bestMoves.add(availableMoves.get(0)); //Worst
					return bestMoves;
				}
				bestMoves.add(availableMoves.get(0));
				return bestMoves;
			}
		}
		return availableMoves;
	}

	private int calcWinner() {
		Card best = trick.get(0); //Index 0 is player to the left of the last player in the trick (player)
		Suit s = best.suit;
		for (int i = 1; i < 3; i++) {
			int curr_player;
			if (i == 1) curr_player = ((player-1)+3)%3;
			else curr_player = player; // this player checking
			Card next = trick.get(i);
			if (next.suit == best.suit && next.rank > best.rank) best = next;
			else if (!playerHasSuit(curr_player,s) && best.suit != Suit.SPADES && next.suit == Suit.SPADES) best = next;
		}
		int won = trick.indexOf(best); //Index tells us which player won.
		if (won == 2) { //this player
			return this.player;
		} else if (won == 0) { //playerNext
			return this.playerNext;
		} else {
			return (this.playerNext+1)%3;
		}
	}

	public int getWins(int playerIndex) { return num_wins[playerIndex]; }

	/**
	 * Moves the current player to the left.
	 */
	private void setPlayerNext () {
		this.player = this.playerNext;
		this.playerNext = (this.player+1)%3;
	}

	/**
	 * Creates an actual copy of this current state.
	 * @return this state in a new object.
	 */
	@Override
	public State clone() {
		State s = new State(this.trick,this.player,this.unseen,this.player_hands[0],this.max_tricks, this.player_has_suit);
		s.player_hands[1] = new LinkedList(this.player_hands[1]);
		s.player_hands[2] = new LinkedList(this.player_hands[2]);
		return s;
	}
}

// Used to store the information set which we determinise from to create a state for the player.
class Node {

	//Controls how important exploration of the tree is wrt. average value of a node.
	private static double EXP_FACTOR = 1.0/Math.sqrt(2);
	int num_wins; //num_wins by this PLAYER
	int num_visits;
	Node parent;
	Card action; // Card to choose to get here.
	List<Node> children;
	int player;

	public Node(Node parent, Card card, int player) {
		this.children = new LinkedList();
		this.num_wins = 0;
		this.num_visits = 0;
		this.parent = parent;
		this.action = card;
		this.player = player;
	}

	public void updateNode(State s) {
		num_visits++;
		if (player != -1)
			num_wins += s.getWins(player);
	}

	public Node selectChild(List<Card> available) {
		List<Node> nodes = new LinkedList();
		for (Node n : children) {
			if (available.contains(n.action)) { nodes.add(n); }
		}
		Collections.sort(nodes,new ISUCTComparator());
		//System.out.println("UCT Scores: ");
		//for (Node n:nodes) System.out.println(n.ISUCT() + " ");
		//System.out.println("Selecting: "+nodes.get(nodes.size()-1).ISUCT());
		return nodes.get(nodes.size()-1);
	}

	/**
	 * Finds which cards have are not children of this node
	 * @param actions
	 * @return
	 */
	public List<Card> unexploredActions(List<Card> actions) {
		LinkedList<Card> unexplored = new LinkedList(actions);
		for (Node n : children) {
			if (unexplored.contains(n.action)) {
				unexplored.remove(n.action);
			}
		}
		return unexplored;
	}

	/**
	 *
	 * @return
	 */
	public double ISUCT() {
		//Depending on time add heuristic (h(i)) which helps choose nodes via h(i)/num_visits.
		return ((double) num_wins /(double) num_visits) + Agent.explore*Math.sqrt(Math.log((double)parent
				.num_visits)/(double) num_visits);
	}

	public Node addChild(Card action, int player) {
		children.add(new Node(this,action,player));
		return children.get(children.size()-1); // Get the last added child.
	}

}

class ISUCTComparator implements Comparator<Node> {

	@Override
	public int compare(Node a, Node b) {
		return Double.compare(a.ISUCT(), b.ISUCT());
		//a.ISUCT() > b.ISUCT() ? -1 : a.ISUCT() < b.ISUCT() ? 1 : 0;
	}

}