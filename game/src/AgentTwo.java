import java.util.List;
import java.util.*;

/**
 * A game playing agent for Moss Side Whist.
 * @author Andre Wang (21714084)
 * @author Sam Heath (21725083)
 */
public class AgentTwo implements MSWAgent {
    private final String NAME = "Conte Marlo Infoset";
    private static final Set<Card> DECK = new HashSet<>(Arrays.asList(Card.values()));
    private static final Suit TRUMP = Suit.SPADES;

    private Map<String, Integer> sboard; // The scoreboard.
    private Set<Card> hand; // What's in my hand right now?
    private Set<Card> seen; // What cards have I seen?
    private Set<Card> unseen; // What cards haven't I seen?
    private Card[] trick = new Card[3]; // three cards in a trick
    private int currMoveInTrick = 0;
    private int playerNum;
    private Map<Integer, EnumMap<Suit, Boolean>> isValidSuit;
    private String playerToLeft;
    private Map<String, Integer> players;

    /**
     * Tells the agent the names of the competing agents, and their relative position.
     */
    @Override
    public void setup(String agentLeft, String agentRight) {
        // TODO greet them or something? I don't know
        // TODO but for realsies, set up data structures here.
        this.playerToLeft = agentLeft;
        players = new HashMap<>();
        players.put(agentLeft, 0);
        players.put(agentRight, 0);
        players.put(NAME, 0);
   }

    /**
     * Starts the round with a deal of the cards.
     * The agent is told the cards they have (16 cards, or 20 if they are the leader)
     * and the order they are playing
     * (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
     */
    @Override
    public void seeHand(List<Card> hand, int order) {
        this.playerNum = order;
        // Set up my hand, as well as instantiate the 'seen' and 'unseen' cards
        this.hand = new HashSet<>(hand);
        this.seen = new HashSet<>(hand);
        this.unseen = new HashSet<>(DECK);
        unseen.removeAll(seen);

        // Reset isValidSuit with the new order
        isValidSuit = instantiateValidSuits(playerNum);

        // Also figure out who's playing when
        for (String s : players.keySet()) {
            if (s.equals(NAME)) players.put(s, order);
            else if (s.equals(playerToLeft)) players.put(s, (order + 1)%3);
            else players.put(s, (order + 2)%3);
        }
    }

    /**
     * Create a map of player numbers to suits they might have.
     * If false, then they definitely do not have the suit; else true.
     * @param myNum my own player number.
     * @return a map of player numbers to whether or not they have a suit.
     */
    private Map<Integer, EnumMap<Suit, Boolean>> instantiateValidSuits(int myNum) {
        Map<Integer, EnumMap<Suit, Boolean>> m = new HashMap<>();
        for (int i = 0; i < 3; i++) { // Three players total
            if (i == myNum) continue; // Don't bother to put ourselves in.
            EnumMap<Suit, Boolean> e = new EnumMap<>(Suit.class);
            for (Suit s : Suit.values()) {
                e.put(s, true);
            }
            m.put(i, e);
        }
        return m;
    }

    private void moveUnseenToSeen(Card c) {
        unseen.remove(c);
        seen.add(c);
    }

    /**
     * This method will be called on the leader agent, after the deal.
     * If the agent is not the leader, it is sufficient to return an empty array.
     */
    @Override
    public Card[] discard() {
        if (this.playerNum != 0) {
            return new Card[]{};
        }
        // Discard greedily, choosing the worst four cards.
        // TODO is there a better way? probably.
        Comparator<Card> cc = new CardComparator(true);
        List<Card> cards = new ArrayList<>(hand);
        cards.sort(cc);
        Card[] discard = new Card[4];
        int numberDiscarded = 0;
        for (int i = 0; i < cards.size() && numberDiscarded < 4; i++) {
            Card curr = cards.get(i);
            // Let's not discard trumps.
            if (curr.suit == TRUMP) {
                continue;
            } else {
                discard[numberDiscarded++] = curr;
            }
        }
        // Update our hand.
        for (Card c : discard) {
            hand.remove(c);
        }
        return discard;
    }

    /**
     * Agent returns the card they wish to play.
     * A 200 ms timelimit is given for this method
     * @return the Card they wish to play.
     */
    @Override
    public Card playCard() {

        // TODO
        return null;
    }

    private Card ISMOMCTreeSearch(int iterations) {
        // TODO
        // For each player, create a single node tree that is
        // representative of our information set.
        MONode[] playerNode = new MONode[3];
        for (int i = 0; i < playerNode.length; i++) {
            // TODO; foreach player create single tree node.
            playerNode[i] = new MONode(null, null, 0);
        }

        return null;
    }

    private MONode ISMOSelect(MONode n) {
        return null;
    }

    /**
     * Sees an Agent play a card.
     * A 50 ms time limit is given to this function.
     * @param card, the Card played.
     * @param agent, the name of the agent who played the card.
     */
    @Override
    public void seeCard(Card card, String agent) {
        // TODO
        trick[currMoveInTrick++] = card;
        moveUnseenToSeen(card);

        if (card.suit != trick[0].suit) {
           // oh-ho-ho! This card doesn't follow suit.
           int pnum = players.get(agent);
           isValidSuit.get(pnum).put(card.suit, false);
        }
    }

    /**
     * See the result of the trick.
     * A 50 ms time limit is given to this method.
     * This method will be called on each agent at the end of each trick.
     * @param winner, the player who played the winning card.
     */
    @Override
    public void seeResult(String winner) {
        // TODO
    }

    /**
     * See the score for each player.
     * A 50 ms time limit is given to this method
     * @param scoreboard, a Map from agent names to their score.
     */
    @Override
    public void seeScore(Map<String, Integer> scoreboard) {
        this.sboard = scoreboard;
        // TODO
    }

    /**
     * Returns the Agents name.
     * A 10ms time limit is given here.
     * This method will only be called once.
     */
    @Override
    public String sayName() {
        return this.NAME;
    }
}
