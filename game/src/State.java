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
		int p1 = 0;
		int p2 = 0;

		//Randomly assign the remaining cards
		Random rand = new Random();
		Collections.shuffle(unseen);

		//cards SHOULD NOT be empty at this stage. As a result players have even number of cards.
		/*if (p1 > p2) {
			player_hands[2].add(cards.remove(rand.nextInt(cards.size())));
			p2++;
		} else {
			player_hands[1].add(cards.remove(rand.nextInt(cards.size())));
			p1++;
		}*/

		if (playerNext != 0 && cards.size() > 0) {
			int k = rand.nextInt(cards.size());
			while (!suitAvail[playerNext][Agent.SUITMAP.get(cards.get(k).suit)]) {
				k = rand.nextInt(cards.size());
			}
			player_hands[2].add(cards.remove(k));
		}

		//This could be dangerous due to while loops.
		int size = cards.size();
		for (int i = 0; i < (size-p1-p2)/2; i++) {
			if (cards.size() == 0) break;
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

}