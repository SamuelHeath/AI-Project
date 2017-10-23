import java.util.*;

/**
 * A node for MO-ISMCTS.
 * @author Andre Wang (21714084)
 * @author Sam Heath (21725083)
 */
public class MONode {
    private MONode p;
    private List<MONode> children;
    private int visitationCount; // why won't my grandchildren visit me?
    private int availabilityCount;
    private double reward;
    private Card move;
    private int whoMoved;
    private Set<Card> movesMadeFromHere;
    public MONode(MONode parent, Card move, int whoIsMoving) {
        this.p = parent;
        this.move = move; // What move got us to this node?
        this.whoMoved = whoIsMoving; // Who picks the action for this node?
        this.visitationCount = 0;
        this.reward = 0;
        this.availabilityCount = 0;
        children = new ArrayList<>();
        movesMadeFromHere = new HashSet<>();
    }

    public Card getMostVisitedChild() {
        int vis = children.get(0).getVisitationCount();
        Card c = children.get(0).getMoveMade();

        for (MONode child : children) {
            if (child.getVisitationCount() > vis) {
                c = child.getMoveMade();
            }
        }
        return c;
    }

    /**
     * @param c a card action of this node
     * @param whoIsMoving who moves after this node?
     */
    public void addChild(Card c, int whoIsMoving) {
        MONode child = new MONode(this, c, whoIsMoving);
        children.add(child);
        movesMadeFromHere.add(c);
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
        if (children.size() < 1) return null;
        MONode child = children.get(0);
        double score = calculateScoreOfChild(child, exploration);
        for (MONode c : children) {
            double currscore = calculateScoreOfChild(c, exploration);
            if (currscore > score) child = c;
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
        double mean = (child.getReward() / child.getVisitationCount());
        double myVisCount = child.getAvailabilityCount();
        double fraction = Math.log(myVisCount) / child.getVisitationCount();
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

    public void setReward(double n) {
        this.reward = n;
    }

    public double getReward() {
        return this.reward;
    }

    public void addToReward(double n) {
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
