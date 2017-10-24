import java.util.*;

/**
 * Represents an instantiation of a particular game state.
 * ie. a determinisation.
 */
public class MOState {
    private List<Card> unseen;
    Map<Integer, Set<Card>> hands;
    private int player;
    private int nextPlayer;
    private Card[] currTrick;
    private int[] tricksWon;
    private final Suit TRUMP = Suit.SPADES;
    private boolean gameOver;
    private int firstPlayer;

    public MOState(int playerNum, int firstPlayer,
                   Set<Card> myHand, Set<Card> unseen, Card[] trick) {
        this.unseen = new ArrayList<>(52);
        this.unseen.addAll(unseen);
        this.tricksWon = new int[3];
        this.gameOver = false;
        this.nextPlayer = playerNum; // If this gets created, I've been asked to play.
        this.player = playerNum;
        this.hands = new HashMap<>(3);
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
        int numberRemainingInUnseen = player == 0 ? 0 : 4;
        int pnum = roundModulus(firstPlayer - 1, 3);
        while (unseen.size() > numberRemainingInUnseen) {
            if (pnum == this.player) {
                pnum = roundModulus(pnum-1, 3);
                continue;
            }
            hands.get(pnum).add(unseen.remove(r.nextInt(unseen.size())));
            pnum = roundModulus(pnum-1,3);
        }
        /*
        for (int c = 0; c < size; c++) {
            // Start allocating at the last player, because they go last in a round
            // so they might be allowed an extra card.
            // TODO In allocation, we should also consider what suits are valid.
            if (pnum == this.player) {
                c--; // de-allocate
                pnum = roundModulus(pnum-1, 3);
                continue;
            }
            hands.get(pnum).add(unseen.get(c));
            pnum = roundModulus(pnum-1,3);
        }
        */
    }

    /**
     * Negative numbers don't wrap back. This makes it wrap back.
     * @param n
     */
    private int roundModulus(int n, int wrap) {
        return ((n%wrap)+wrap)%wrap;
    }

    /**
     * @return The score of playerNum.
     */
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
    private void incrementToNextPlayer()
    {
        this.nextPlayer = (this.nextPlayer + 1)%3;
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
            }
            else if (c.suit == TRUMP) {
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
