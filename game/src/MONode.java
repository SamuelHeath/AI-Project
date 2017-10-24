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
     * @param whoIsMoving who moves at this node?
     */
    public MONode addChild(Card c, int whoIsMoving) {
        MONode child = new MONode(this, c, whoIsMoving);
        children.add(child);
        movesMadeFromHere.add(c);
        return child;
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
