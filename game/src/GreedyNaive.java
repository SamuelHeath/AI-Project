import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class GreedyNaive implements MSWAgent
{
	private String name;
	private List<Card> seenCards; //stores the current hand of the player.
	private int turn;
	private List<Card> currentHand; //stores the cards in player's hand.
	private List<Card> hearts;
	private List<Card> diamonds;
	private List<Card> clubs;
	private List<Card> spades;
	private Comparator<Card> cardComparator;

	public GreedyNaive(String name)
	{
		this.name = name;
		this.turn = 0;
		this.seenCards = new LinkedList<Card>();
		this.currentHand = new LinkedList<Card>();
		this.clubs = new LinkedList<Card>();
		this.spades = new LinkedList<Card>();
		this.hearts = new LinkedList<Card>();
		this.diamonds = new LinkedList<Card>();
		initCardComparator();
	}


	/**
	 * Initializes the comparator object for ranking a list of cards
	 * from lowest to highest values.
	 */
	private void initCardComparator()
	{
		this.cardComparator = new Comparator<Card>(){
			public int compare(Card c1, Card c2)
			{
				int mult1 = 1;
				int mult2 = 1;
				if(c1.suit == Suit.SPADES)
					mult1 = 100;
				if(c2.suit == Suit.SPADES)
					mult2 = 100;
				int score1 = mult1 * c1.rank;
				int score2 = mult2 * c2.rank;
				return score1 - score2;
			}
		};
	}


	/**
	 * Compare 2 cards..
	 * @param c1
	 * @param c2
	 * @return
	 */
	public int compare(Card c1, Card c2)
	{
		//System.out.println("* Comparing between... "+c1.toString() + " & " + c2.toString());
		int mult1 = 1;
		int mult2 = 1;
		if(c1.suit == Suit.SPADES)
			mult1 = 100;
		if(c2.suit == Suit.SPADES)
			mult2 = 100;
		int score1 = mult1 * c1.rank;
		int score2 = mult2 * c2.rank;
		return score1 - score2;
	}


	/**
	 * Tells the agent the names of the competing agents, and their relative position.
	 * */
	public void setup(String agentLeft, String agentRight)
	{
		//TODO
	}

	/**
	 * Starts the round with a deal of the cards.
	 * The agent is told the cards they have (16 cards, or 20 if they are the leader)
	 * and the order they are playing (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
	 */
	public void seeHand(List<Card> hand, int order)
	{
		this.currentHand = hand;
		putCardsToSuitList();
	}


	/**
	 * Print discarded cards.
	 * @param dc discarded cards array of size 4.
	 */
	private void printDiscardArray(Card[] dc)
	{
		System.out.println("-----------------\nDiscarded\n-----------------");
		for(int i = 0; i < dc.length; i++)
		{
			System.out.println(dc[i].toString());
		}

	}

	//draw out strong spades of opponents by playing weak spades. Look into this...

	/**
	 * Cards are allocated to their relevant list of suits.
	 */
	private void putCardsToSuitList()
	{
		clubs.clear(); diamonds.clear(); hearts.clear(); spades.clear();
		for(int i = 0; i < this.currentHand.size(); i++)
		{
			Card c = this.currentHand.get(i);
			if(c.suit == Suit.CLUBS)
				clubs.add(c);
			else if(c.suit == Suit.DIAMONDS)
				diamonds.add(c);
			else if(c.suit == Suit.HEARTS)
				hearts.add(c);
			else
				spades.add(c);
		}
	}

	/**
	 * This method will be called on the leader agent, after the deal.
	 * If the agent is not the leader, it is sufficient to return an empty array.
	 */
	public Card[] discard()
	{
		Collections.sort(currentHand, cardComparator);

		Card discardArr[] = new Card[4];
		for(int i = 0; i < discardArr.length; i++)
		{
			discardArr[i] = currentHand.remove(0);
		}
		//printDiscardArray(discardArr);
		return discardArr;
	}

	/**
	 * Returns the largest suit in Agent's hand.
	 * @return
	 */
	private List<Card> findLargestSuitInHand()
	{
		Collections.sort(this.hearts, cardComparator); Collections.sort(this.diamonds, cardComparator);
		Collections.sort(this.spades, cardComparator); Collections.sort(this.clubs, cardComparator);
		List<Card> ls = ((this.spades.size() > this.clubs.size()) ? this.spades : this.clubs);
		ls = ((ls.size() > this.hearts.size()) ?
				ls : this.hearts);
		ls = ((ls.size() > this.diamonds.size()) ?
				ls : this.diamonds);
		return ls;
	}


	/**
	 * Check to see if a round has been completed. I.e. all 3 cards have been played.
	 */
	private void checkForRoundCompletion() {
		if(this.turn == 3)
		{
			this.turn = 0;
			seenCards.clear();
			//System.out.println("\n========Trick Complete=======\n");
		}
	}



	private void printHand()
	{
		System.out.println("The hand : ");
		for(int i = 0; i < this.currentHand.size(); i++)
		{
			System.out.print(this.currentHand.get(i).toString() + " ");
		}
		System.out.println("");
	}



	/**
	 * Agent returns the card they wish to play.
	 * A 200 ms timelimit is given for this method
	 * @return the Card they wish to play.
	 */
	public Card playCard()
	{
		//printHand();
		checkForRoundCompletion();
		List<Card> playingSuitReference = findLargestSuitInHand();
		Card pc = null; //play card
		// we are the leader --- if spades is the largest suit in hand, play the weakest spade first??
		if(this.turn == 0){
			if(playingSuitReference.get(0).suit == Suit.SPADES)
			{
				pc = this.spades.remove(0); //weakest spade.
				this.currentHand.remove(pc);
			}
			else
			{
				pc = playingSuitReference.remove(playingSuitReference.size()-1);
				this.currentHand.remove(pc);
			}
		}else {
			Suit targetSuit = seenCards.get(0).suit;
			Collections.sort(seenCards, cardComparator);
			if(targetSuit == Suit.CLUBS) {playingSuitReference = clubs;}
			else if(targetSuit == Suit.SPADES) {playingSuitReference = spades;}
			else if(targetSuit == Suit.HEARTS) {playingSuitReference = hearts;}
			else {playingSuitReference = diamonds;}

			// don't have the suit we should play.
			if(playingSuitReference.size() == 0){
				Collections.sort(currentHand, cardComparator);
				if(spades.size() == 0){
					pc = currentHand.remove(0); //the weakest card we have...
				}else {

					Collections.sort(spades, cardComparator); //Spades clause... try and win.
					if(this.compare( spades.get(spades.size()-1), seenCards.get(seenCards.size()-1)) < 0)
						pc = currentHand.remove(0);
					else {
						pc = spades.remove(0);
						currentHand.remove(pc);
					}
				} // else we do have the suit that the leader played. And must play from that...
			} else {
				boolean playCardFound = false;
				Collections.sort(playingSuitReference, cardComparator);
				for(int i = 0; i < playingSuitReference.size(); i++)
				{
					if( this.compare(playingSuitReference.get(i), seenCards.get(seenCards.size()-1)) < 0 )
						continue;
					else
					{
						pc = playingSuitReference.remove(i); //remove smallest required to win...
						this.currentHand.remove(pc);
						//System.out.println("* Ha! found one. " + pc.toString());
						playCardFound = true;
						break;
					}
				}
				if(!playCardFound) {
					pc = playingSuitReference.get(0);
					//System.out.println("* Meh.. You win. " + pc.toString());
					this.currentHand.remove(pc);
				}
			}
		}
		//printHand();
		putCardsToSuitList();
		this.turn++;
		return pc;
	}



	/**
	 * Sees an Agent play a card.
	 * A 50 ms timelimit is given to this function.
	 * @param card, the Card played.
	 * @param agent, the name of the agent who played the card.
	 */
	public void seeCard(Card card, String agent)
	{
		if(agent.equals(this.name) == false)
		{
			checkForRoundCompletion();
			seenCards.add(card);
			this.turn++;
		}
	}

	/**
	 * See the result of the trick.
	 * A 50 ms timelimit is given to this method.
	 * This method will be called on each eagent at the end of each trick.
	 * @param winner, the player who played the winning card.
	 * */

	public void seeResult(String winner)
	{
		//TODO
	}

	/**
	 * See the score for each player.
	 * A 50 ms timelimit is givien to this method
	 * @param scoreboard, a Map from agent names to their score.
	 **/
	public void seeScore(Map<String, Integer> scoreboard)
	{
		//TODO
	}

	/**
	 * Returns the Agents name.
	 * A 10ms timelimit is given here.
	 * This method will only be called once.
	 */
	public String sayName()
	{
		return this.name;
	}
}
