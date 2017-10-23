import java.util.*;

/**
 * Created by Sam on 23-Sep-17.
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

	public State(List<Card> trick, int cur_player, List<Card> unseen, List<Card> my_hand, int max_depth) {
		this.trick = new LinkedList(trick);
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
	public void determinise(Boolean[][] suitAvail) {
		List<Card> cards = unseen;
		//Build-up decks based on information we have gained!

		//Randomly assign the remaining cards
		Random rand = new Random();
		Collections.shuffle(unseen);

		List<Card> cardsForBoth = new LinkedList();

		int size;
		if (Agent.lead) size = cards.size();
		else size = cards.size()-4; //We havent seen which cards were thrown away
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
			Suit s = first.suit;
			List<Card> playable = new LinkedList();
			boolean has_suit = false;
			for (Card c : hand) {
				if (c.suit == s) {
					has_suit = true;
					playable.add(c);
				} else if (c.suit == Suit.SPADES)
					playable.add(c);
			}
			if (!has_suit) return hand; //We can choose any card, some better than others, e.g. if they
			// cant win this round play a crap card.
			return playable;
		}
	}

	public boolean canGoDeepa() { return this.num_tricks >= this.max_tricks; }

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
			//printScores();
			this.trick.clear();
			//System.out.println();
			this.num_tricks++;
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
		for (Card c:player_hands[player_index]) {
			if (c.suit == searchSuit) return true;
		}
		return false;
	}

	public List<Card> getWinningCards() {
		return getWinningCards(this.availableActions());
	}

	public List<Card> getWinningCards(List<Card> availableMoves) {
		if (trick.size() == 0) {
			List<Card> bestMoves = new LinkedList();
			int player_next1 = (player+1)%3;
			int player_next2 = (player+2)%3;
			for (Card c:availableMoves) {
				if (c.suit != Suit.SPADES) {
					//Convert PlayerHasSuit to bool lookup
					if (!playerHasSuit(player_next1, c.suit) && !playerHasSuit(player_next2, c.suit) &&
							!playerHasSuit(player_next1,Suit.SPADES) && !playerHasSuit(player_next1,Suit.SPADES)) {
						bestMoves.add(c);
					}
				} else {
					if (!playerHasSuit(player_next1, c.suit) && !playerHasSuit(player_next2, c.suit)) bestMoves.add(c);
				}
			}
			if (bestMoves.size() > 0) return bestMoves;
			else return availableMoves;
		}
		Card toBeat = trick.get(0);
		if (trick.size() == 1) {
			int next_player = (player+1)%3;
			List<Card> hand = player_hands[next_player];
			List<Card> bestMoves = new LinkedList();
			if (playerHasSuit(next_player,toBeat.suit)) {

			} else if (toBeat.suit != Suit.SPADES && playerHasSuit(next_player,Suit.SPADES)) {
				Card bestNextCard = getBestCardFromSuit(next_player,Suit.SPADES);
				for (Card c:availableMoves) {
					if (c.suit == Suit.SPADES && c.rank > bestNextCard.rank)bestMoves.add(c);
				}
				return bestMoves; //Could be no best moves
			}
		}
		if (trick.size() == 2) { // 2 cards Played ur last player
			Card c1 = trick.get(1);
			if (c1.suit != toBeat.suit && c1.suit == Suit.SPADES) {
				if (!playerHasSuit(((player-1)+3)%3,toBeat.suit)) toBeat = c1; // Only if the player didnt have the suit will we update the winning card.
			} else if (c1.suit == toBeat.suit && c1.rank > toBeat.rank) {
				toBeat = c1;
			}
		}
		List<Card> good_moves = new LinkedList();
		for (Card c:availableMoves) {
			if (c.suit != toBeat.suit && c.suit == Suit.SPADES) {
				good_moves.add(c);
			} else if (c.suit == toBeat.suit && c.rank > toBeat.rank) {
				good_moves.add(c);
			}
		}
		if (good_moves.size() > 0) Collections.sort(good_moves,new CardComparator(true));
		return good_moves;
	}

	/**
	 *
	 * @return
	 */
	public Card getMove() {
		Card best;
		if (this.trick.size() > 0 && playerHasSuit(player,trick.get(0).suit)) {
			List<Card> winners = getWinningCards();
			if (trick.size() == 2 && winners.size() > 0) {
				return winners.get(winners.size()-1);
			}
			List<Card> actions = availableActions();
			Random rand = new Random();
			best = actions.get(rand.nextInt(actions.size()));
			for (Card c:actions) {
				if (best.rank < c.rank) best = c;
			}
		} else {
			List<Card> hand = player_hands[player];
			Collections.sort(hand, new CardComparator(true));
			best = hand.get(0);
		}
		return best;
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

	@Override
	public State clone() {
		State s = new State(this.trick,this.player,this.unseen,this.player_hands[0],this.max_tricks);
		s.player_hands[1] = new LinkedList(this.player_hands[1]);
		s.player_hands[2] = new LinkedList(this.player_hands[2]);
		return s;
	}
}