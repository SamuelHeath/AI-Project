import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sam on 23-Sep-17.
 */
public class Node {

	//Controls how important exploration of the tree is wrt. average value of a node.
	private static double EXP_FACTOR = 1.0/Math.sqrt(2);
	int num_wins; //num_wins by this PLAYER
	int num_visits;
	Node parent;
	Card action; // Card to choose to get here.
	List<Node> children;
	int player;

	public Node(Node parent, Card c, int player) {
		this.children = new LinkedList();
		this.num_wins = 0;
		this.num_visits = 0;
		this.parent = parent;
		this.action = c;
		this.player = player;
	}

	public void updateNode(State s) {
		num_visits++;
		if (player != -1)
			num_wins += s.getWins(player);
	}

	public Node selectChild(List<Card> available) {
		List<Node> nodes = new LinkedList();
		for (Node n : children) {
			if (!available.contains(n.action)) { nodes.add(n); }
		}
		return Collections.max(nodes, new ISUCTComparator());
	}

	/**
	 * Finds which cards have are not children of this node
	 * @param actions
	 * @return
	 */
	public List<Card> unexploredActions(List<Card> actions) {
		for (Node n : children) {
			if (actions.contains(n.action)) {
				actions.remove(n.action);
			}
		}
		return actions;
	}

	/**
	 *
	 * @return
	 */
	public double ISUCT() {
		//Depending on time add heuristic (h(i)) which helps choose nodes via h(i)/num_visits.
		return (double) num_wins /(double) num_visits + EXP_FACTOR*Math.sqrt(Math.log((double)parent.num_visits)/
				(double) num_visits);
	}

	public Node addChild(Card action, int player) {
		children.add(new Node(this,action,player));
		return children.get(children.size()-1); // Get the last added child.
	}

}

class ISUCTComparator implements Comparator<Node> {

	@Override
	public int compare(Node a, Node b) {
		return Double.compare(a.ISUCT(), b.ISUCT());
		//a.ISUCT() > b.ISUCT() ? -1 : a.ISUCT() < b.ISUCT() ? 1 : 0;
	}

}