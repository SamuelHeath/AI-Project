import java.util.*;

public class RandomAgent implements MSWAgent{

  public static final int LEADER = 0;
  public static final int LEFT = 1;
  public static final int RIGHT = 2;

  private List<Card> hand;
  private Random rand = new Random();

  /**
   * Tells the agent the names of the competing agents, and their relative position.
   * */
  public void setup(String agentLeft, String agentRight){
    //I'm Random, i don't care
  }

  /**
   * Starts the round with a deal of the cards.
   * The agent is told the cards they have (16 cards, or 20 if they are the leader)
   * and the order they are playing (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
   * */
  public void seeHand(List<Card> hand, int order){
    this.hand  = hand;
  }

  /**
   * This method will be called on the leader agent, after the deal.
   * If the agent is not the leader, it is sufficient to return an empty array.
   */
  public Card[] discard(){
    Card[] discard = new Card[4];
    for(int i = 0; i<4;i++)
      discard[i] = hand.remove(rand.nextInt(20-i));
    return discard;
  }

  /**
   * Agent returns the card they wish to play.
   * A 200 ms timelimit is given for this method
   * @return the Card they wish to play.
   * */
  public Card playCard(){
    return hand.remove(rand.nextInt(hand.size()));
  }

  /**
   * Sees an Agent play a card.
   * A 50 ms timelimit is given to this function.
   * @param card, the Card played.
   * @param agent, the name of the agent who played the card.
   * */
  public void seeCard(Card card, String agent){
    //I'm Random, i don't care
  }

  /**
   * See the result of the trick. 
   * A 50 ms timelimit is given to this method.
   * This method will be called on each eagent at the end of each trick.
   * @param winner, the player who played the winning card.
   * */
  public void seeResult(String winner){
    //I'm Random, i don't care
  }

  /**
   * See the score for each player.
   * A 50 ms timelimit is givien to this method
   * @param scoreboard, a Map from agent names to their score.
   **/
  public void seeScore(Map<String, Integer> scoreboard){
    //I'm Random, i don't care
  }

  /**
   * Returns the Agents name.
   * A 10ms timelimit is given here.
   * This method will only be called once.
   * */
  public String sayName(){
    String name= "";
    for(int i = 0; i<6; i++)
      name+=(char)('A'+rand.nextInt(26));
    return name;
  }

}
