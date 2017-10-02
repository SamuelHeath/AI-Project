import java.util.*;

/**
 * Created by Sam on 23-Sep-17.
 */
class State {

	int[] num_wins;
	int num_moves; // Number of moves of this trick
	String playerNext;
	List<Card> trick;
	List<Card>[] player_hands = new List[3];
	Random r = new Random();
	int max_tricks;
	List<Card> unseen;

	public State(List<Card> trick, String next_player, List<Card> unseen, List<Card> my_hand) {
		num_moves = trick.size();
		playerNext = next_player;
		this.unseen = unseen;
		player_hands[0] = my_hand;
		max_tricks = 6; // 18 Moves ahead
	}

	/**
	 * Determinise the root game state by taking the Imperfect Game Info and convert it into a Perfect
	 * Info game.
	 */
	public void determinise(Boolean[][] suitAvail) {
		List<Card> cards = unseen;
		//Build-up decks based on information we have gained!
		int p1 = 0;
		int p2 = 0;

		//Randomly assign the remaining cards
		Random rand = new Random();
		Collections.shuffle(unseen);

		//cards SHOULD NOT be empty at this stage. As a result players have even number of cards.
		if (p1 > p2) {
			player_hands[2].add(cards.remove(rand.nextInt(cards.size())));
			p2++;
		} else {
			player_hands[1].add(cards.remove(rand.nextInt(cards.size())));
			p1++;
		}

		//This could be dangerous due to while loops.
		for (int i = 0; i < (cards.size()-p1-p2)/2; i++) {
			int k = rand.nextInt(cards.size());
			//if player 1 is able to accept this suit then give them this card
			while (!suitAvail[1][Agent.SUITMAP.get(cards.get(k).suit)]) {
				k = rand.nextInt(cards.size());
			}
			player_hands[1].add(cards.remove(k));
			k = rand.nextInt(cards.size());
			while (!suitAvail[2][Agent.SUITMAP.get(cards.get(k).suit)]) {
				k = rand.nextInt(cards.size());
			}
			player_hands[2].add(cards.remove(k));
		}
	}

	/**
	 * For the current player at this state point what moves can they do?
	 */
	public List<Card> availableActions() {
		List<Card> hand = player_hands[Agent.AGENTMAP.get(playerNext)];
		if (num_moves == 0) {
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
		player_hands[Agent.AGENTMAP.get(this.playerNext)].remove(action);
		trick.add(action);
		if (trick.size() == 3) {
			//Calc who wins and so who starts the round.
			calcWinner();
		} else {
			//Next player is the player to the left of the current player
		}
	}

	private int calcWinner() {
		Card best = trick.get(0);
		Suit s = best.suit;
		for (int i = 1; i < 3; i++) {
			Card next = trick.get(i);
			if (next.suit == best.suit && next.rank > best.rank) best = next;
			else if (best.suit != Suit.SPADES && next.suit == Suit.SPADES) best = next;
		}
		return trick.indexOf(best); //Index tells us which player won.
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
	}

}