import java.util.Comparator;

class CardComparator implements Comparator<Card> {
    boolean sortBy;

    public CardComparator(boolean sortByRank) {
        sortBy = sortByRank;
    }

    @Override
    public int compare(Card a, Card b) {
        int x;
        if (sortBy) {
            x = a.rank < b.rank ? 1 : a.rank == b.rank ? 0 : -1;
            if (x ==0) {
                return -1*a.suit.toString().compareTo(b.suit.toString());
            } else return x;
        }
        else return a.toString().compareTo(b.toString());
    }
}