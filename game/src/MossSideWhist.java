import java.util.*;
import java.io.*;

public class MossSideWhist{

  private String leader; //the name of the first player
  private String left; //the name of the second player
  private String right; //the name of the third player
  private Map<String, MSWAgent> agents;  //a map from names to players
  private Map<String, Integer> scoreboard;  //a map from names to scores
  private Map<String, ArrayList<Card>> hands;  //a map from names to cards
  private Random rand = new Random();
  private PrintStream report;

  /**
   * Constructor. Takes three agents and initialises variables
   * */
  public MossSideWhist(MSWAgent p1, String n1, MSWAgent p2, String n2, MSWAgent p3, String n3){
    //get the players to say their names
    leader = n1;left = n2;right = n3;;
    //store agents
    agents = new HashMap<String, MSWAgent>();
    agents.put(leader, p1);agents.put(left, p2);agents.put(right, p3);
    //set scoreboard to 0
    scoreboard = new HashMap<String, Integer>();
    scoreboard.put(leader, 0);scoreboard.put(left, 0);scoreboard.put(right, 0);
    //inform players of neighbours.
    p1.setup(left, right);p2.setup(right, leader);p3.setup(leader, left);
  }


  public void playGame(int rounds){
    for(int i = 0; i<3*rounds; i++){
      playHand();
      String tmp = leader; leader = left; left = right; right = tmp;
    }
  }

  public void playHand(){
  report.println("The leader is "+leader+", to the left is "+left+" and "+right+" is to the right.");  
    deal();
  display(leader); display(left); display(right);  
    Card[] discard = agents.get(leader).discard();
    for(int i = 0; i<4; i++){
      if(i>discard.length || !hands.get(leader).remove(discard[i]))
        hands.get(leader).remove(0);
    }
    String first = leader;
    for(int i = 0; i<16; i++){
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
   * Deals cards to players
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
      hands.get(leader).add(deck.remove(rand.nextInt(deck.size())));
      hands.get(left).add(deck.remove(rand.nextInt(deck.size())));
      hands.get(right).add(deck.remove(rand.nextInt(deck.size())));
    }
    Collections.sort(hands.get(leader));
    Collections.sort(hands.get(left));
    Collections.sort(hands.get(right));
    agents.get(leader).seeHand(hands.get(leader), MSWAgent.LEADER);
    agents.get(left).seeHand(hands.get(left), MSWAgent.LEFT);
    agents.get(right).seeHand(hands.get(right), MSWAgent.RIGHT);
  }

  /**
   * Logic for one trick of the game.
   * Each agent plays a card in turn.
   * If an agent plays an illegal card, 
   * a random legal card from their hand is played in its place. 
   * A points penalty may also be applied.
   * Returns the name of the winner.
   * */
  public String trick(String first){
    String second = left; String third = right;
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
  report.println(next);  
    String winner = getWinner(lead, next, last, first, second, third);
    agents.get(leader).seeResult(winner);
    agents.get(left).seeResult(winner);
    agents.get(right).seeResult(winner);
  report.println(winner+" wins the trick!");  
    return winner;
  }

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

  private boolean followsSuit(Card c1, Card c2){
    return c2.suit==c1.suit || c2.suit ==Suit.SPADES;
  }

  private void showCards(Card c, String player){
    agents.get(leader).seeCard(c, player);
    agents.get(left).seeCard(c, player);
    agents.get(right).seeCard(c, player);
  }
  
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

  private void display(String agent){
    display(agent, false);
  }

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

  private void showScores(){
    report.println(leader+": "+scoreboard.get(leader));
    report.println(left+": "+scoreboard.get(left));
    report.println(right+": "+scoreboard.get(right));
  }

  public static void main(String[] args){
  }
}

