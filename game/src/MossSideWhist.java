import java.util.*;
import java.io.*;

public class MossSideWhist{

  private String leader; //the name of the first player
  private String left; //the name of the second player
  private String right; //the name of the third player
  private Map<String, MSWAgent> agents;  //a map from names to players
  private Map<String, Integer> scoreboard;  //a map from names to scores
  private Map<String, ArrayList<Card>> hands;  //a map from names to cards
  private Random rand = new Random();  //using for dealing cards
  private PrintStream report;  //For debugging. Can show hands and moves of each agent to stdout, 
                               //or can be replaced by a stub to hide the full game state.

  /**
   * Constructor. Takes three agents and their names, and initialises variables
   * @param p1 the class of the first agent (will be leader in the first round)
   * @param p2 the class of the second agent
   * @param p3 the class of the third agent
   * @param n1 the name of the first agent, must be different to n2 and n3, otherwise names reassigned
   * @param n2 the name of the first agent, must be different to n1 and n3, otherwise names reassigned
   * @param n3 the name of the first agent, must be different to n2 and n1, otherwise names reassigned
   * */
  public MossSideWhist(MSWAgent p1, String n1, MSWAgent p2, String n2, MSWAgent p3, String n3){
    //assign names
    leader = n1;
    left = n2.equals(n1)?n1+" copy":n2;
    right = n3.equals(n1)?n1+" duplicate":n3.equals(n2)?n2+" copy":n3;
    //store agents
    agents = new HashMap<String, MSWAgent>();
    agents.put(leader, p1);agents.put(left, p2);agents.put(right, p3);
    //set scoreboard to 0
    scoreboard = new HashMap<String, Integer>();
    scoreboard.put(leader, 0);scoreboard.put(left, 0);scoreboard.put(right, 0);
    //inform players of neighbours.
    p1.setup(left, right);p2.setup(right, leader);p3.setup(leader, left);
  }

  /**
   * Constructor. Takes three agents, and initialises variables. 
   * Names will be taken fromm agents sayName method
   * @param p1 the class of the first agent (will be leader in the first round)
   * @param p2 the class of the second agent
   * @param p3 the class of the third agent
   * */
  public MossSideWhist(MSWAgent p1, MSWAgent p2, MSWAgent p3){
    this(p1, p1.sayName(), p2, p2.sayName(), p3, p3.sayName());
  }

  /**
   * Plays the specified number of full rotations of the game.
   * Scoreboard is updated and results are dsplayed to the printStream.
   * After each hand the leader role moves 1 space ot the left.
   * @param rotations, the number of full rotations (i.e. it plays 3*rotations rounds of MossSideWhist)
   * @param report, a printstream to display to game state.
   **/
  public void playGame(int rounds, PrintStream report){
    this.report = report;
    for(int i = 0; i<3*rounds; i++){
      playHand();
      String tmp = leader; leader = left; left = right; right = tmp;
    }
  }

  //Maybe should pass in the report as an argument?
  /**
   * The Logic for playing a hand of Moss Side Whist.
   * Cards are dealt, the leader discards and then all tricks are played, 
   * until no more cards remain.
   * */
  public void playHand(){
  report.println("The leader is "+leader+", to the left is "+left+" and "+right+" is to the right.");  
    deal();
  display(leader); display(left); display(right);  
    Card[] discard = agents.get(leader).discard();
    for(int i = 0; i<4; i++){
      if(i>discard.length || !hands.get(leader).remove(discard[i]))
        hands.get(leader).remove(0);//if illegitimate discards, the 0 card is discarded.
        //could include a score penalty here as well.
        display(leader);
    }
    String first = leader;
    for(int i = 0; i<16; i++){
  display(leader); display(left); display(right);  
      first = trick(first);
      scoreboard.put(first, scoreboard.get(first)+1);
    }
    scoreboard.put(leader, scoreboard.get(leader)-8);
    scoreboard.put(left, scoreboard.get(left)-4);
    scoreboard.put(right, scoreboard.get(right)-4);
    agents.get(leader).seeScore(scoreboard); 
    agents.get(left).seeScore(scoreboard); 
    agents.get(right).seeScore(scoreboard);
  showScores();  
  }

  /**
   * Deals cards to players.
   * The Hands data structure is updated with a random deal of cards
   * with 20 to the leader and 16 to the remaining two players.
   * */
  public void deal(){
    hands = new HashMap<String, ArrayList<Card>>();
    hands.put(leader, new ArrayList<Card>());
    hands.put(left, new ArrayList<Card>());
    hands.put(right, new ArrayList<Card>());
    ArrayList<Card> deck = new ArrayList<Card>(Arrays.asList(Card.values()));
    for(int i = 0; i<4; i++)
      hands.get(leader).add(deck.remove(rand.nextInt(deck.size())));
    for(int i = 0; i<16; i++){
      hands.get(left).add(deck.remove(rand.nextInt(deck.size())));
      hands.get(right).add(deck.remove(rand.nextInt(deck.size())));
      hands.get(leader).add(deck.remove(rand.nextInt(deck.size())));
    }
    Collections.sort(hands.get(leader));
    Collections.sort(hands.get(left));
    Collections.sort(hands.get(right));
    agents.get(leader).seeHand((ArrayList<Card>)hands.get(leader).clone(), MSWAgent.LEADER);
    agents.get(left).seeHand((ArrayList<Card>)hands.get(left).clone(), MSWAgent.LEFT);
    agents.get(right).seeHand((ArrayList<Card>)hands.get(right).clone(), MSWAgent.RIGHT);
  }

  /**
   * Logic for one trick of the game.
   * Each agent plays a card in turn.
   * If an agent plays an illegal card, 
   * a random legal card from their hand is played in its place. 
   * A points penalty may also be applied.
   * Returns the name of the winner.
   * @param first the name of the first player to play a card in this trick
   * */
  public String trick(String first){
    String second = left; String third = right;//calculate the position of each player.
    if(first.equals(left)){second= right; third = leader;}
    if(first.equals(right)){second=leader; third = left;}
    Card[] trick = new Card[3];
  display(first, true);
    Card lead = agents.get(first).playCard();
    ArrayList<Card> hand = hands.get(first);
    if(!hand.remove(lead))
      lead = hand.remove(rand.nextInt(hand.size()));
    showCards(lead, first);
  report.println(lead);
  display(second, true);  
    Card next = agents.get(second).playCard();
    hand = hands.get(second);
    while(!legal(next, second, lead.suit))
      next = hand.get(rand.nextInt(hand.size()));
    hand.remove(next);
    showCards(next, second);
  report.println(next);
  display(third, true);  
    Card last = agents.get(third).playCard();
    hand = hands.get(third);
    while(!legal(last, third, lead.suit))
      last = hand.get(rand.nextInt(hand.size()));
    hand.remove(last);
    showCards(last, third);
  report.println(last);  
    String winner = getWinner(lead, next, last, first, second, third);
    agents.get(leader).seeResult(winner);
    agents.get(left).seeResult(winner);
    agents.get(right).seeResult(winner);
  report.println(winner+" wins the trick!");  
    return winner;
  }

  //calculates the winner of a trick
  private String getWinner(Card lead, Card next, Card last, String first, String second, String third){
    if(lead.compareTo(next)<0 || !followsSuit(lead, next)){//first beats second
      if(lead.compareTo(last)<0 || !followsSuit(lead, last)){//first beats third
        return first;
      }
      else{//third beats first
        return third;
      }
    }
    else{//second beats first
      if(next.compareTo(last)<0 || ! followsSuit(lead, last)){//second beats thirs
        return second;
      }
      else{//third beats second
        return third;
      }
    }
  }  

  //checks to see if a card follows suit, or is a trump
  private boolean followsSuit(Card c1, Card c2){
    return c2.suit==c1.suit || c2.suit ==Suit.SPADES;
  }

  //calls the see methods for each agent in the game,
  //letting them know the card that was played.
  private void showCards(Card c, String player){
    agents.get(leader).seeCard(c, player);
    agents.get(left).seeCard(c, player);
    agents.get(right).seeCard(c, player);
  }
  
  //checks to see if the played card was legal
  private boolean legal(Card played, String player, Suit suit){
    if(hands.get(player).contains(played)){
      if(played.suit!=suit){
        for(Card c: hands.get(player)){
          if(c.suit==suit) return false; // card didn't match suit, but had one in hand.
        }
        return true;// card didn't match suit, but not in hand
      }
      return true; // card in hand and matched suit
    }
    return false; //card not in hand.      
  }

  //overloaded method to display an agents full hand
  private void display(String agent){
    display(agent, false);
  }

  //displays the agents name and score, and if hidden is false, the remaining cards in their hand.
  private void display(String agent, boolean hidden){
    report.println("Player: "+agent+"\tScore: "+scoreboard.get(agent));
    if(!hidden){
      String hand = "";
      for(Card c: hands.get(agent)){
        hand+= c+",";
      }
      report.println(hand);
    }
  }

  //shows the scores of all players.
  private void showScores(){
    report.println(leader+": "+scoreboard.get(leader));
    report.println(left+": "+scoreboard.get(left));
    report.println(right+": "+scoreboard.get(right));
  }

  public static void main(String[] args){
    MossSideWhist game = new MossSideWhist(new RandomAgent(), new RandomAgent(), new RandomAgent());
    game.playGame(1, System.out);
  }
}

