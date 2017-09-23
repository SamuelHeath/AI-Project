import java.util.*;

public interface MSWAgent{

  public static final int LEADER = 0;
  public static final int LEFT = 1;
  public static final int RIGHT = 2;

  /**
   * Tells the agent the names of the competing agents, and their relative position.
   * */
  public void setup(String agentLeft, String agentRight);

  /**
   * Starts the round with a deal of the cards.
   * The agent is told the cards they have (16 cards, or 20 if they are the leader)
   * and the order they are playing (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
   * */
  public void seeHand(List<Card> hand, int order);

  /**
   * This method will be called on the leader agent, after the deal.
   * If the agent is not the leader, it is sufficient to return an empty array.
   */
  public Card[] discard();

  /**
   * Agent returns the card they wish to play.
   * A 200 ms timelimit is given for this method
   * @return the Card they wish to play.
   * */
  public Card playCard();
  /**
   * Sees an Agent play a card.
   * A 50 ms timelimit is given to this function.
   * @param card, the Card played.
   * @param agent, the name of the agent who played the card.
   * */
  public void seeCard(Card card, String agent);

  /**
   * See the result of the trick. 
   * A 50 ms timelimit is given to this method.
   * This method will be called on each eagent at the end of each trick.
   * @param winner, the player who played the winning card.
   * */
  public void seeResult(String winner);

  /**
   * See the score for each player.
   * A 50 ms timelimit is givien to this method
   * @param scoreboard, a Map from agent names to their score.
   **/
  public void seeScore(Map<String, Integer> scoreboard);

  /**
   * Returns the Agents name.
   * A 10ms timelimit is given here.
   * This method will only be called once.
   * */
  public String sayName();

}
