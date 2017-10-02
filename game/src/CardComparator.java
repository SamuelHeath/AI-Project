import java.util.Comparator;

class CardComparator implements Comparator<Card> {
    boolean sortBy;

    public CardComparator(boolean sortByRank) {
        sortBy = sortByRank;
    }

    @Override
    public int compare(Card a, Card b) {
        if (sortBy) return a.rank < b.rank ? -1 : a.rank == b.rank ? 0 : 1;
        else return a.toString().compareTo(b.toString());
    }
}