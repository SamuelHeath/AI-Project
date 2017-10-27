import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Sam Heath (21725083)
 * @author Andre Wang (21714084)
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

	public Node(Node parent, Card card, int player) {
		this.children = new LinkedList();
		this.num_wins = 0;
		this.num_visits = 0;
		this.parent = parent;
		this.action = card;
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
			if (available.contains(n.action)) { nodes.add(n); }
		}
		Collections.sort(nodes,new ISUCTComparator());
		//System.out.println("UCT Scores: ");
		//for (Node n:nodes) System.out.println(n.ISUCT() + " ");
		//System.out.println("Selecting: "+nodes.get(nodes.size()-1).ISUCT());
		return nodes.get(nodes.size()-1);
	}

	/**
	 * Finds which cards have are not children of this node
	 * @param actions
	 * @return
	 */
	public List<Card> unexploredActions(List<Card> actions) {
		LinkedList<Card> unexplored = new LinkedList(actions);
		for (Node n : children) {
			if (unexplored.contains(n.action)) {
				unexplored.remove(n.action);
			}
		}
		return unexplored;
	}

	/**
	 *
	 * @return
	 */
	public double ISUCT() {
		//Depending on time add heuristic (h(i)) which helps choose nodes via h(i)/num_visits.
		return ((double) num_wins /(double) num_visits) + Agent.explore*Math.sqrt(Math.log((double)parent
				.num_visits)/(double) num_visits);
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