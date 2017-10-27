import java.util.List;
import java.util.*;

/**
 * A game playing agent for Moss Side Whist.
 * @author Andre Wang (21714084)
 * @author Sam Heath (21725083)
 */
public class Agent21714084 implements MSWAgent {
    private final String NAME = "Conte_Marlo_Infoset";
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
        Comparator<Card> cc = new MyCardComparator();
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
        Card c = ISMOMCTreeSearch(195, true);
        return c;
    }

    /**
     * If time is true, then do up to 'iterations' wall-time milliseconds of search.
     * Otherwise, do that many iterations of search.
     * @param iterations number of iterations (or milliseconds)
     * @param time whether or not walltime should be used to terminate
     * @return A card to play next
     */
    private Card ISMOMCTreeSearch(int iterations, boolean time) {
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
                    selectChild(s.getMoves(), 0.5);
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
        trick[players.get(agent)] = card;
        moveUnseenToSeen(card);
        // If my card, then remove it from my hand.
        if (agent == this.NAME) {
            hand.remove(card);
        }
        if (card.suit != trick[firstPlayer].suit) {
            // oh-ho-ho! This card doesn't follow suit.
            int pnum = players.get(agent);
            if (pnum != playerNum) {
                isValidSuit.get(pnum).put(trick[firstPlayer].suit, false);
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


/**
 * A node for MO-ISMCTS.
 * @author Andre Wang (21714084)
 * @author Sam Heath (21725083)
 */
class MONode {
    private MONode p;
    private List<MONode> children;
    private int visitationCount; // why won't my grandchildren visit me?
    private int availabilityCount;
    private int reward;
    private Card move;
    private int whoMoved;
    private Set<Card> movesMadeFromHere;
    public MONode(MONode parent, Card move, int whoMadeActionToHere) {
        this.p = parent;
        this.move = move; // What move got us to this node?
        this.whoMoved = whoMadeActionToHere; // Who picks the action for this node?
        this.visitationCount = 0;
        this.reward = 0;
        this.availabilityCount = 1; // If instantiated, then available!
        children = new ArrayList<>();
        movesMadeFromHere = new HashSet<>();
    }

    public Card getMostVisitedChild() {
        int vis = children.get(0).getVisitationCount();
        Card c = children.get(0).getMoveMade();
        for (MONode child : children) {
            if (child.getVisitationCount() > vis) {
                c = child.getMoveMade();
                vis = child.getVisitationCount();
            }
        }
        return c;
    }

    public List<MONode> getChildren() {
        return children;
    }

    /**
     * If there exists a child of this node that corresponds
     * to this action, then return it.
     * Otherwise, create and return such a child.
     * @param action a particular action
     * @param whoIsMoving who's moving at this node
     * @return a child node of this action
     */
    public MONode findOrCreateChild(Card action, int whoIsMoving) {
        // Is there one already?
        for (MONode child : children) {
            if (child.getMoveMade().equals(action)) {
                return child;
            }
        }
        // Otherwise, create child.
        return this.addChild(action, whoIsMoving);
    }

    /**
     * @param c a card action of this node
     * @param whoIsMoving who moved to this node?
     */
    public MONode addChild(Card c, int whoIsMoving) {
        MONode child = new MONode(this, c, whoIsMoving);
        children.add(child);
        movesMadeFromHere.add(c);
        return child;
    }

    public int getWhoMoved() {
        return this.whoMoved;
    }

    /**
     * Use the default exploration constant of 0.7.
     * @param validMoves the moves valid from this position.
     * @return a selected child via UCB1
     */
    public MONode selectChild(List<Card> validMoves) {
        return selectChild(validMoves, 0.7);
    }

    /**
     * Select a child using the algorithm described in Cowling 2012.
     * (ie. a variation of UCB1).
     * The usual UCB1 is
     * mean + c * math.sqrt(ln(parent visitation count) / selection from parent)
     * Cowling 2012 modifies the parent visitation count to also include
     * the availability count.
     * @param validMoves The moves valid from this node
     * @param exploration the exploration constant. Cowling 2012 uses 0.7
     * @return the selected child node.
     */
    public MONode selectChild(List<Card> validMoves, double exploration) {
        Set<Card> canDo = new HashSet<>(validMoves);
        int score = Integer.MIN_VALUE;
        MONode child = null;
        for (MONode c : children) {
            if (canDo.contains(c.getMoveMade())) {
                double currScore = calculateScoreOfChild(c, exploration);
                if (currScore > score) child = c;
                c.addToAvailabilityCount(1);
            }
        }
        // TODO
        return child;
    }

    /**
     * Using UCB1, calculate the 'weight' of this child
     * to decide whether or not it will be The Chosen One (ie. selected)
     * @param child a particular child
     * @return the modified UCB1
     */
    private double calculateScoreOfChild(MONode child, double exploration) {
        double mean = child.getReward() / child.getVisitationCount();
        double myVisCount = child.getAvailabilityCount();
        double fraction = Math.sqrt(Math.log(myVisCount) / child.getVisitationCount());
        return mean + (exploration * fraction);
    }

    /**
     * Check what moves are possible
     * @param validMoves those moves possible from this node
     * @return
     */
    public List<Card> getUntriedMoves(List<Card> validMoves) {
        List<Card> moves = new ArrayList<>(16);
        for (Card c : validMoves) {
            if (!movesMadeFromHere.contains(c)) moves.add(c);
        }
        return moves;
    }

    public MONode getParent() {
        return this.p;
    }

    public void addToVisitCount(int n) {
        this.visitationCount += n;
    }

    public void addToAvailabilityCount(int n) {
        this.availabilityCount += n;
    }

    public void setReward(int n) {
        this.reward = n;
    }

    public double getReward() {
        return this.reward;
    }

    public void addToReward(int n) {
        this.reward += n;
    }

    public int getVisitationCount() {
        return this.visitationCount;
    }

    public int getAvailabilityCount() {
        return this.availabilityCount;
    }

    public Card getMoveMade() {
        return this.move;
    }

}

/**
 * Represents an instantiation of a particular game state.
 * ie. a determinisation.
 * @author Andre Wang (21714084)
 * @author Sam Heath (21725083)
 */
class MOState {
    private List<Card> unseen;
    private Map<Integer, Set<Card>> hands;
    private int player;
    private int nextPlayer;
    private Card[] currTrick;
    private int[] tricksWon;
    private final Suit TRUMP = Suit.SPADES;
    private boolean gameOver;
    private int firstPlayer;
    private Map<Integer, EnumMap<Suit, Boolean>> isValidSuit;
    private int numberRemoved;

    public MOState(int playerNum, int firstPlayer,
                   Set<Card> myHand, Set<Card> unseen, Card[] trick,
                   Map<Integer, EnumMap<Suit, Boolean>> isValidSuit, int numberRemoved) {
        this.unseen = new ArrayList<>(52);
        this.unseen.addAll(unseen);
        this.tricksWon = new int[3];
        this.gameOver = false;
        this.nextPlayer = playerNum; // If this gets created, I've been asked to play.
        this.player = playerNum;
        this.hands = new HashMap<>(3);
        this.isValidSuit = isValidSuit;
        this.numberRemoved = numberRemoved;
        for (int i = 0; i < 3; i++) {
            this.hands.put(i, new HashSet<>());
        }
        this.hands.put(playerNum, new HashSet<>(myHand));
        this.firstPlayer = firstPlayer;
        randomizeCards();
        this.currTrick = trick.clone();
    }

    /**
     * Randomly allocate cards to the other hands.
     */
    private void randomizeCards() {
        Collections.shuffle(unseen);
        Random r = new Random();
        // If I'm the leader, then I know that the set of unseen cards
        // is the set of cards allocated to the opponents.
        // Otherwise, account for discards.
        int numberRemainingInUnseen = player == 0 ? 0 : 4 - numberRemoved;

        // Random, arbitrary allocation that doesn't consider
        // our current knowledge.
        int pnum = firstPlayer;
        while (unseen.size() > numberRemainingInUnseen) {
            pnum = roundModulus(pnum - 1, 3);
            // Skip me - I have my cards!
            if (pnum == this.player) {
                pnum = roundModulus(pnum - 1, 3);
            }
            hands.get(pnum).add(unseen.remove(r.nextInt(unseen.size())));
        }
        // Iterative repair - we should 'fix up' any cards
        // that shouldn't exist in a particular opponent's hand.
        // see: n-queens problem (kind of?)
        // pnum is one opponent; we need to figure out other opponent's number
        int other = pnum != 0 && this.player != 0 ? 0 :
                pnum != 1 && this.player != 1 ? 1 : 2;

        // All cards that can be used to swap
        List<Collection<Card>> allCards = new ArrayList<>();
        allCards.add(hands.get(pnum));
        allCards.add(hands.get(other));
        allCards.add(unseen);
        while (!checkHandValidity(isValidSuit.get(pnum), hands.get(pnum)) ||
                !checkHandValidity(isValidSuit.get(other), hands.get(other))) {
            findCardsToSwapWith(allCards, hands.get(pnum),
                    getInvalidCards(hands.get(pnum), isValidSuit.get(pnum)),
                    isValidSuit.get(pnum));
            findCardsToSwapWith(allCards, hands.get(other),
                    getInvalidCards(hands.get(other), isValidSuit.get(other)),
                    isValidSuit.get(other));
        }
    }

    private void findCardsToSwapWith(List<Collection<Card>> all,
                                     Set<Card> hand, List<Card> remove, EnumMap<Suit, Boolean> validSuit) {
        for (Card c : remove) {
            boolean foundCandidate = false;
            // Search through allCards
            // to see if there's something I can swap with
            for (Collection<Card> col : all) {
                if (col == hand) { // don't swap with self
                    continue;
                }
                // Generate a new 'view'
                List<Card> currList = new ArrayList<>(col);
                for (Card candidate : currList) {
                    if (validSuit.get(candidate.suit)) {
                        // swap
                        foundCandidate = true;
                        col.remove(candidate);
                        hand.add(candidate);
                        hand.remove(c);
                        col.add(c);
                        break;
                    }
                }
                if (foundCandidate) break;
            }
        }
    }


    private boolean checkHandValidity(EnumMap<Suit, Boolean> validSuits, Set<Card> hand) {
        for (Card c : hand) {
            if (!validSuits.get(c.suit)) return false;
        }
        return true;
    }

    private List<Card> getInvalidCards(Set<Card> hand, EnumMap<Suit, Boolean> validSuits) {
        List<Card> l = new ArrayList<>(hand.size());
        for (Card c : hand) {
            if (!validSuits.get(c.suit)) l.add(c);
        }
        return l;
    }
    /**
     * Return the first card found that is invalid
     * else any arbitrary card.
     * @param validSuits the valid suits for this player
     * @param otherSuits the valid suits for the other player
     *                   this param just helps guide choice of a card to swap
     * @param hand the player's current hand
     * @return
     */
    private Card getBadCard(EnumMap<Suit, Boolean> validSuits, EnumMap<Suit, Boolean> otherSuits,
                            Set<Card> hand) {
        // Get the first card that shouldn't be in this hand.
        for (Card c : hand) {
            if (!validSuits.get(c.suit)) {
                return c;
            }
        }
        // No such card? Try to get one that is allowed in the other hand
        for (Card c : hand) {
            if (otherSuits.get(c.suit)) return c;
        }
        // Otherwise, just return any card
        // If we get up to this point, it's a bit concerning...
        Random rand = new Random();
        List<Card> handlist = new ArrayList<>(hand);
        return handlist.get(rand.nextInt(handlist.size()));
    }

    /**
     * Negative numbers don't wrap back. This makes it wrap back.
     *
     * @param n
     */
    private int roundModulus(int n, int wrap) {
        return ((n % wrap) + wrap) % wrap;
    }

    /**
     * @return The scores.
     */
    public int[] getScore() {
        return tricksWon.clone();
    }

    /**
     * The current player makes a move.
     *
     * @param c the card they play
     */
    public void move(Card c) {
        currTrick[nextPlayer] = c;
        hands.get(nextPlayer).remove(c);
        incrementToNextPlayer();

        // A trick is over if the next player has already played.
        if (currTrick[nextPlayer] != null) {
            int win = winner(currTrick);
            tricksWon[win]++;
            currTrick = new Card[3]; // reset trick
            setNextPlayer(win);
            firstPlayer = win;
            // No cards left? Then it's game over!
            // Let's look at the scores ...
            if (getMoves().isEmpty()) {
                gameOver = true;
                tricksWon[0] = tricksWon[0] - 8;
                tricksWon[1] = tricksWon[1] - 4;
                tricksWon[2] = tricksWon[2] - 4;
            }
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Get the winner of a trick
     *
     * @param t, an array of played cards where the indices are player number.
     * @return the index of the winning card
     */
    private int winner(Card[] t) {
        Comparator<Card> cc = new MyCardComparator();
        int topInd = 0;
        for (int i = 1; i < 3; i++) {
            if (cc.compare(t[topInd], t[i]) < 0 &&
                    (t[i].suit == t[0].suit || t[i].suit == Suit.SPADES)) {
                topInd = i;
            }
        }
        return topInd;
    }

    private void setNextPlayer(int n) {
        this.nextPlayer = n;
    }

    private void incrementToNextPlayer() {
        this.nextPlayer = (this.nextPlayer + 1) % 3;
    }

    public int getCurrentPlayer() {
        return this.nextPlayer;
    }

    /**
     * Get all valid moves. These are:
     * If leader, then all cards.
     * Otherwise:
     * Cards of the same suit as the first card
     * If no such card exists, then spades, if any exists
     * Else, empty list.
     *
     * @return all valid moves from the current player.
     */
    public List<Card> getMoves() {
        Set<Card> currPlayerHand = hands.get(nextPlayer);
        // Is the current player the first player?
        // If so, then they can play any card they want!
        if (currTrick[firstPlayer] == null) {
            return new ArrayList<>(currPlayerHand);
        }

        // Otherwise, they are quite restricted.
        Suit validSuit = currTrick[firstPlayer].suit;
        List<Card> sameSuit = new ArrayList<>(16);
        List<Card> spades = new ArrayList<>(16);

        // Iterate over all cards in the player's hand,
        // adding to the relevant list.
        for (Card c : currPlayerHand) {
            if (c.suit == validSuit) {
                sameSuit.add(c);
            } else if (c.suit == TRUMP) {
                spades.add(c);
            }
        }

        // Now, let's see what we can play!
        // If we have at least one of the same suit, then return those cards.
        // Otherwise, we let them play spades!
        // ... Otherwise, just do whatever you want.
        if (sameSuit.size() > 0) return sameSuit;
        else if (spades.size() > 0) return spades;
        else return new ArrayList<>(currPlayerHand);
    }

    public List<Card> getHand(int i) {
        return new ArrayList<>(hands.get(i));
    }


    public static void main(String[] args) {
    }
}

class MyCardComparator implements Comparator<Card> {
    private final Suit TRUMP = Suit.SPADES;

    @Override
    public int compare(Card o1, Card o2) {
        // Deal with special cases first.
        if (o1.suit == TRUMP && o2.suit != TRUMP) return 1;
        if (o1.suit != TRUMP && o2.suit == TRUMP) return -1;

        // If neither, or both are spades, then compare by rank instead.
        return o1.rank - o2.rank;
    }
}
