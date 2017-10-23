import java.util.*;

/**
 * Represents an instantiation of a particular game state.
 * ie. a determinisation.
 */
public class MOState {
    private List<Card> unseen;
    Map<Integer, Set<Card>> hands;
    private int player;
    private int nextPlayer = 0; // 0 is the leader, by default
    private Card[] currTrick;
    private int[] tricksWon;
    private final Suit TRUMP = Suit.SPADES;

    public MOState(int playerNum, Set<Card> myHand, Set<Card> unseen, Card[] trick) {
        this.unseen = new ArrayList<>(52);
        this.unseen.addAll(unseen);
        this.player = playerNum;
        hands = new HashMap<>(3);
        hands.put(playerNum, myHand);
        randomizeCards();
        this.currTrick = trick;
        tricksWon = new int[3];
    }

    /**
     * Randomly allocate cards to the other hands.
     */
    private void randomizeCards() {
        Collections.shuffle(unseen);
        int size = unseen.size() - 4; // four cards were removed.
        int pnum = 2;
        for (int c = 0; c < size; c++) {
            // Start allocating at player 2, because they go last in a round
            // so they might be allowed an extra card.
            if (pnum == this.player) continue;
            hands.get(pnum).add(unseen.get(c++));
            pnum = (pnum-1)%3;
        }
    }

    public int getScore() {
        return tricksWon[player];
    }

    /**
     * The current player makes a move.
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

            // No cards left? Then it's game over!
            // Let's look at the scores ...
            tricksWon[0] = tricksWon[0] - 8;
            tricksWon[1] = tricksWon[1] - 4;
            tricksWon[2] = tricksWon[2] - 4;
        }
    }

    /**
     * Get the winner of a trick
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
        this.nextPlayer = (this.nextPlayer + 1)%3;
    }

    /**
     * Get all valid moves. These are:
     * If leader, then all cards.
     * Otherwise:
     * Cards of the same suit as the first card
     * If no such card exists, then spades, if any exists
     * Else, empty list.
     * @return all valid moves from the current player.
     */
    public List<Card> getMoves() {
        Set<Card> currPlayerHand = hands.get(nextPlayer);
        // Is the current player the leader?
        // If so, then they can play any card they want!
        if (nextPlayer == 0) {
            return new ArrayList<>(currPlayerHand);
        }

        // Otherwise, they are quite restricted.
        Suit validSuit = currTrick[0].suit;
        List<Card> sameSuit = new ArrayList<>(16);
        List<Card> spades = new ArrayList<>(16);

        // Iterate over all cards in the player's hand,
        // adding to the relevant list.
        for (Card c : currPlayerHand) {
            if (c.suit == validSuit) {
                sameSuit.add(c);
            }
            else if (c.suit == TRUMP) {
                spades.add(c);
            }
        }

        // Now, let's see what we can play!
        // If we have at least one of the same suit, then return those cards.
        // Otherwise, we return the spades (which may be an empty list).
        if (sameSuit.size() > 0) return sameSuit;
        else return spades;
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
