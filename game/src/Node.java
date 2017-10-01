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
	int wins;
	int visits;
	Node parent;
	Card action; // Card to choose to get here.
	List<Node> children;

	public Node(Node parent, Card c) {
		this.wins = 0;
		this.visits = 0;
		this.parent = parent;
		this.action = c;
	}

	public void updateNode(State s) {
		visits++;
		wins += s.getWins(0);
	}


	public Node selectChild(List<Card> available) {
		List<Node> nodes = new LinkedList();
		for (Node n : children) {
			if (!available.contains(n.action)) { nodes.add(n); }
		}
		return Collections.max(nodes, new ISUCTComparator());
	}

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
		//Depending on time add heuristic (h(i)) which helps choose nodes via h(i)/visits.
		return (double)wins/(double)visits + EXP_FACTOR*Math.sqrt(Math.log((double)parent.visits)/
				(double)visits);
	}

	public Node addChild(Card action) {
		children.add(new Node(this,action));
		return children.get(children.size()-1); // Get the last added child.
	}

}

class ISUCTComparator implements Comparator<Node> {

	@Override
	public int compare(Node a, Node b) {
		return a.ISUCT() > b.ISUCT() ? -1 : a.ISUCT() < b.ISUCT() ? 1 : 0;
	}

}