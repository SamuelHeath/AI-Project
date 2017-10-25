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
    private int firstPlayer;
    private int numberRemoved;

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
        this.firstPlayer = 0;
        // Set up my hand, as well as instantiate the 'seen' and 'unseen' cards
        this.hand = new HashSet<>(hand);
        this.seen = new HashSet<>(hand);
        this.unseen = new HashSet<>(DECK);
        unseen.removeAll(seen);
        numberRemoved = 0;

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
        // TODO is there a better way? probably. Even MCTS here?
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
        long start = System.currentTimeMillis();
        Card c = ISMOMCTreeSearch(190, true);
        //System.out.println((System.currentTimeMillis() - start));
        return c;
    }

    /**
     * If time is true, then do up to 'iterations' wall-time milliseconds of search.
     * Otherwise, do that many iterations of search.
     * @param iterations
     * @param time
     * @return A card to play next
     */
    private Card ISMOMCTreeSearch(int iterations, boolean time) {
        // TODO
        // For each player, create a single node tree that is
        // representative of our information set.
        Random rng = new Random();
        // To ease my pain, let's store two copies; one represents the root
        MONode[][] playerNodes = new MONode[3][2];
        for (int i = 0; i < playerNodes.length; i++) {
            playerNodes[i][0] = new MONode(null, null, -1); // root
            playerNodes[i][1] = playerNodes[i][0]; // leaf
        }

        if (time) { // if we use time to figure out when to return
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < iterations) {
                MOState state = new MOState(playerNum, firstPlayer, hand,
                        unseen, trick, isValidSuit, numberRemoved);
                treeSearchIteration(state, playerNodes, rng);
            }
        }
        else {
            // For n iterations ...
            for (int i = 0; i < iterations; i++) {
                MOState state = new MOState(playerNum, firstPlayer,
                        hand, unseen, trick, isValidSuit, numberRemoved);
                treeSearchIteration(state, playerNodes, rng);
            }
        }

        // Finally, return the action that has been explored the most.
        return playerNodes[playerNum][0].getMostVisitedChild();
    }

    private void treeSearchIteration(MOState s, MONode[][] playerNodes, Random r) {
        select(s, playerNodes);
        expand(s, playerNodes, r);
        int[] worth = simulate(s, r);
        for (int j = 0; j < playerNodes.length; j++) {
            playerNodes[j][1] = backpropagate(playerNodes[j][1], worth);
        }
    }


    /**
     * Pick a path that seems good.
     * @param s the game state (immediately after determinisation)
     * @param playerNodes the player node array
     */
    private void select(MOState s, MONode[][] playerNodes) {
        while (!s.isGameOver() &&
                playerNodes[s.getCurrentPlayer()][1].
                        getUntriedMoves(s.getMoves()).isEmpty()) {
            // The current player picks an action from possible moves using UCB1
            MONode n = playerNodes[s.getCurrentPlayer()][1].
                    selectChild(s.getMoves());
            Card action = n.getMoveMade();
            // Update every tree.
            for (int j = 0; j < playerNodes.length; j++) {
                playerNodes[j][1] = playerNodes[j][1].findOrCreateChild(action,
                        s.getCurrentPlayer());
            }
            s.move(action); // update the determinisation with this action
        }
    }

    /**
     * Select any node that hasn't been tried out yet.
     * @param s the game state, after selection
     * @param playerNodes the player node array
     * @param r a Random object
     */
    private void expand(MOState s, MONode[][] playerNodes, Random r) {
        List<Card> untried = playerNodes[s.getCurrentPlayer()][1].
                getUntriedMoves(s.getMoves());
        if (!untried.isEmpty()) {
            Card act = untried.get(r.nextInt(untried.size()));
            for (int i = 0; i < playerNodes.length; i++) {
                playerNodes[i][1] = playerNodes[i][1].findOrCreateChild(act,
                        s.getCurrentPlayer());
            }
            s.move(act);
        }
    }
    /**
     * A monte-carlo simulation.
     * @param s the current game state
     * @param r for RNG.
     * @return the utility of a terminal state
     */
    private int[] simulate(MOState s, Random r) {
        while (!s.isGameOver()) {
            List<Card> possAct = s.getMoves();
            Card act = possAct.get(r.nextInt(possAct.size()));
            s.move(act);
        }
        return s.getScore();
    }

    /**
     * Back-propagate visitation values etc.
     * @param leaf the final leaf of the tree in this iteration
     * @param rewards the rewards for each player
     *                as observed through simulation
     * @return the root node
     */
    private MONode backpropagate(MONode leaf, int[] rewards) {
        MONode curr = leaf;
        while (curr.getParent() != null) {
            curr.addToVisitCount(1);
            curr.addToReward(rewards[curr.getWhoMoved()]);
            // node availability updated during creation of node - easier
            curr = curr.getParent();
        }
        return curr;
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
        trick[players.get(agent)] = card;
        moveUnseenToSeen(card);
        //System.out.println(agent + " played " + card);
        // If my card, then remove it from my hand.
        if (agent == this.NAME) {
            hand.remove(card);
        }
        if (card.suit != trick[firstPlayer].suit) {
            // oh-ho-ho! This card doesn't follow suit.
            int pnum = players.get(agent);
            if (pnum != playerNum) {
                isValidSuit.get(pnum).put(trick[firstPlayer].suit, false);
                //System.out.println(pnum + " doesn't have " + trick[firstPlayer].suit);
                // CHECK: if both opponents don't have a particular suit
                // then let's remove those suits from unseen.
                for (int k : isValidSuit.keySet()) {
                    if (k == pnum) continue;
                    if (!isValidSuit.get(k).get(trick[firstPlayer].suit) &&
                            !isValidSuit.get(pnum).get(trick[firstPlayer].suit)) {
                        // Remove all such suits from unseen.
                        // This lets us know what was discarded.
                        List<Card> toBeRemoved = new ArrayList<>(16);
                        for (Card c : unseen) {
                            if (c.suit == trick[firstPlayer].suit) toBeRemoved.add(c);
                        }
                        //System.out.println(toBeRemoved.size() + " cards removed");
                        numberRemoved += toBeRemoved.size();
                        unseen.removeAll(toBeRemoved);
                    }
                }
            }
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
        // Reset the tricks.
        currMoveInTrick = 0;
        trick = new Card[3];
        firstPlayer = players.get(winner);
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
