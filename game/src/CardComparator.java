import java.util.Comparator;
/**
 * @author Sam Heath (21725083)
 * @author Andre Wang (21714084)
 */
class CardComparator implements Comparator<Card> {
    boolean sortBy; // sort by rank first?
    boolean lowestWinningCard = false;

    public CardComparator(boolean sortByRank) {
        sortBy = sortByRank;
    }

    public CardComparator(boolean sortByRank, boolean lowestSpade) {
        sortBy = sortByRank;
        lowestWinningCard = lowestSpade;
    }

    @Override
    public int compare(Card a, Card b) {
        int x;
        if (sortBy) {
             x = Integer.compare(a.rank, b.rank);
             //x = a.rank < b.rank ? 1 : a.rank == b.rank ? 0 : -1;
            if (x == 0) {
                return a.suit.toString().compareTo(b.suit.toString());
            }
            else return x;
        }
        else if (lowestWinningCard) {
            x = Integer.compare(a.rank,b.rank);
            if (x == 0) {
                return -1*a.suit.toString().compareTo(b.suit.toString());
            } else return x;
        }
        else return a.toString().compareTo(b.toString());
    }
}