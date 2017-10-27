import java.util.*;

/**
 * @author Sam Heath (21725083)
 * @author Andre Wang (21714084)
 */
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
		if (!Agent.lead && size <= 36 && size >= 32) { // You aren't the leader and u may not have
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
			if (suitAvail[1][Agent.SUITMAP.get(cards.get(i).suit)] && suitAvail[2][Agent.SUITMAP.get(cards.get(i).suit)]) {
				cardsForBoth.add(cards.get(i));
			} else if (suitAvail[1][Agent.SUITMAP.get(cards.get(i).suit)] && !suitAvail[2][Agent.SUITMAP.get(cards.get(i).suit)]) {
				player_hands[1].add(cards.get(i));
			} else if (!suitAvail[1][Agent.SUITMAP.get(cards.get(i).suit)] && suitAvail[2][Agent.SUITMAP.get(cards.get(i).suit)]) {
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
		int player_next1 = (player+1)%3;
		int player_next2 = (player+2)%3;
		if (trick.size() == 0) {
			Card highest = availableMoves.get(availableMoves.size()-1); //Play highest?
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
								null && getBestCardFromSuit(player_next1, c.suit).rank < c.rank) {
							bestMoves.add(c);
						}
					} else { //Play the best card or lowest spade to draw out others spades
						if (playerHasSuit(player,Suit.SPADES)) {
							if (c.suit == Suit.SPADES && spade == null) spade = c;
							if (spade != null && spade.rank > c.rank && c.suit == Suit.SPADES) {
								spade = c; //Play lowest Spade to draw out the other plays spades and hopefully do
								// above move.
							}
						}
						if (c.rank > highest.rank && highest!=spade) highest = c;
					}
			}
			if (!bestMoves.contains(highest)) {
				//if (player==0)System.out.println("Highest: " + highest.toString());
				bestMoves.add(highest);
			}
			if (!bestMoves.contains(spade) && spade!=null) {
				//if (player==0)System.out.println("Lowest Spade: " +  spade.toString());
				bestMoves.add(spade); // Lowest spade
			}
			if (bestMoves.size() > 0) return bestMoves;
			else return availableMoves;
		}
		Card toBeat = trick.get(0);
		Card orig = trick.get(0);
		Collections.sort(availableMoves);
		if (trick.size() == 1) {
			if (playerHasSuit(player,orig.suit)) { //original suit
				Card worst = availableMoves.get(0);
				for (Card c:availableMoves) {
					if (c.suit == orig.suit) { //If we can play this
						if (playerHasSuit(playerNext,orig.suit)) {
							if (getBestCardFromSuit(player_next1,orig.suit) != null && getBestCardFromSuit(playerNext, orig.suit)
									.rank< c.rank) {
								bestMoves.add(c);
							} else {
								if (worst.rank > c.rank && c.suit != Suit.SPADES) worst = c;
							}
							//If the next player doesnt have the correct suit
						} else {
							//Wrong suit but they can play spades
							if (playerHasSuit(playerNext,Suit.SPADES)) {
								if (worst.rank > c.rank && c.suit != Suit.SPADES) worst = c; //Here we lose worst
							} else {
								if (worst.rank > c.rank && c.suit != Suit.SPADES) worst = c; //Here we win with worst
							}
						}
					}
				}
				if (bestMoves.size() == 0) bestMoves.add(worst);
				return bestMoves;
			} else {
				if (toBeat.suit != Suit.SPADES && !playerHasSuit(player,toBeat.suit) && playerHasSuit
						(playerNext,Suit.SPADES)) {
					Card bestNextCard = getBestCardFromSuit(playerNext, Suit.SPADES);
					for (Card c : availableMoves) {
						if (bestNextCard != null && c.suit == Suit.SPADES && c.rank > bestNextCard.rank)
							bestMoves.add(c);
					}
					return bestMoves; //Could be no best moves
				}
			}
		} else if (trick.size() == 2) { // 2 cards Played ur last player
			Card c1 = trick.get(1);
			if (c1.suit != orig.suit) {
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
					if (bestMoves.size() > 0) Collections.sort(bestMoves, new CardComparator(true));
					return bestMoves;
				}
				//play worst move
			} else {
				if (playerHasSuit(player, Suit.SPADES)) {
					//If we are able to beat the last player then play worst card that wins
					if (c1.suit == Suit.SPADES && getBestCardFromSuit(player,Suit.SPADES) != null &&
							getBestCardFromSuit(player,Suit.SPADES).rank > c1.rank) {
						for (Card c : availableMoves) { //For all cards find all that can win
							if (c.suit == Suit.SPADES && c.rank > c1.rank) {
								bestMoves.add(c);
							}
						}
						return bestMoves;
					}
				} else {
					bestMoves.add(availableMoves.get(0)); //Worst
					return bestMoves;
				}
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