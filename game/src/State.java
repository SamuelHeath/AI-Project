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
	int max_tricks;
	List<Card> unseen;

	public State(List<Card> trick, int cur_player, List<Card> unseen, List<Card> my_hand) {
		this.trick = new LinkedList(trick);
		num_moves = trick.size();
		player = cur_player;
		playerNext = (this.player+1)%3;
		this.unseen = new LinkedList(unseen);
		player_hands[0] = new LinkedList(my_hand);
		player_hands[1] = new LinkedList();
		player_hands[2] = new LinkedList();
		max_tricks = 6; // 18 Moves ahead
		num_wins = new int[] {0,0,0};
	}

	/**
	 * Determinise the root game state by taking the Imperfect Game Info and convert it into a Perfect
	 * Info game.
	 */
	public void determinise(Boolean[][] suitAvail) {
		List<Card> cards = unseen;
		System.out.println("Cards to Dist: "+cards.size());
		//Build-up decks based on information we have gained!

		//Randomly assign the remaining cards
		Random rand = new Random();
		Collections.shuffle(unseen);

		for (int i = 0; i < 3; i++) {
			System.out.print("Player: " + i);
			for (int j = 0; j < 4; j ++) {
				System.out.print(" Suit: " + j + " ");
				System.out.print(suitAvail[i][j]);
			}
			System.out.println();
		}

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
				player_hands[1].add(cardsForBoth.get(k));
			} else if (player_hands[1].size() > player_hands[2].size()) {
				player_hands[2].add(cardsForBoth.get(k));
			} else {
				player_hands[1].add(cardsForBoth.get(k));
			}
		}
		System.out.println("Player 1: " + player_hands[1].size() + " Player 2 " + player_hands[2].size());
	}

	/**
	 * For the current player at this state point what moves can they do?
	 */
	public List<Card> availableActions() {
		List<Card> hand = player_hands[player];
		if (trick.size() == 0) {
			return hand;
		} else {
			Card first = trick.get(0);
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

	/**
	 * From this current state point the player performs an action (plays a card)
	 * @param action
	 */
	public void performAction(Card action) {
		this.player_hands[this.player].remove(action);
		this.unseen.remove(action);

		trick.add(action);
		if (trick.size() == 3) {
			//Calc who num_wins and so who starts the round.
			num_wins[calcWinner()]++;
			trick.clear();
		} else {
			setPlayerNext();
		}
	}

	private int calcWinner() {
		Card best = trick.get(0); //Index 0 is player to the left of current player
		Suit s = best.suit;
		for (int i = 1; i < 3; i++) {
			Card next = trick.get(i);
			if (next.suit == best.suit && next.rank > best.rank) best = next;
			else if (best.suit != Suit.SPADES && next.suit == Suit.SPADES) best = next;
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

	public int getNumTricks() {
		int sum=0;
		for (int i : num_wins) sum+=i;
		return sum;
	}

	/**
	 * Chooses who is the next player.
	 */
	private void setPlayerNext () {
		this.player = this.playerNext;
		this.playerNext = (this.player+1)%3;
	}

	@Override
	public State clone() {
		State s = new State(this.trick,this.player,this.unseen,this.player_hands[0]);
		s.player_hands[1] = new LinkedList(this.player_hands[1]);
		s.player_hands[2] = new LinkedList(this.player_hands[2]);
		return s;
	}
}