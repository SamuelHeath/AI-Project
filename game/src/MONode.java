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
    public MONode(MONode parent, Card move, int whoIsMoving) {
        this.p = parent;
        this.move = move;
        this.whoMoved = whoIsMoving;
        this.visitationCount = 0;
        this.reward = 0;
        this.availabilityCount = 0;
        children = new ArrayList<>();
    }

    /**
     * @param c a card action of this node
     * @param whoIsMoving who moves after this node?
     */
    public void addChild(Card c, int whoIsMoving) {
        MONode child = new MONode(this, c, whoIsMoving);
    }

    public MONode selectChild() {
        // TODO
        return null;
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

    public int getVisitationCount() {
        return this.visitationCount;
    }

    public int getAvailabilityCount() {
        return this.availabilityCount;
    }


}
