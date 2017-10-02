enum Suit{ HEARTS, CLUBS, DIAMONDS, SPADES}

public enum Card{
  
  ACE_S(14, Suit.SPADES),
  KING_S(13, Suit.SPADES),
  QUEEN_S(12, Suit.SPADES),
  JACK_S(11, Suit.SPADES),
  TEN_S(10, Suit.SPADES),
  NINE_S(9, Suit.SPADES),
  EIGHT_S(8, Suit.SPADES),
  SEVEN_S(7, Suit.SPADES),
  SIX_S(6, Suit.SPADES),
  FIVE_S(5, Suit.SPADES),
  FOUR_S(4, Suit.SPADES),
  THREE_S(3, Suit.SPADES),
  TWO_S(2, Suit.SPADES),
  ACE_D(14, Suit.DIAMONDS),
  KING_D(13, Suit.DIAMONDS),
  QUEEN_D(12, Suit.DIAMONDS),
  JACK_D(11, Suit.DIAMONDS),
  TEN_D(10, Suit.DIAMONDS),
  NINE_D(9, Suit.DIAMONDS),
  EIGHT_D(8, Suit.DIAMONDS),
  SEVEN_D(7, Suit.DIAMONDS),
  SIX_D(6, Suit.DIAMONDS),
  FIVE_D(5, Suit.DIAMONDS),
  FOUR_D(4, Suit.DIAMONDS),
  THREE_D(3, Suit.DIAMONDS),
  TWO_D(2, Suit.DIAMONDS),
  ACE_C(14, Suit.CLUBS),
  KING_C(13, Suit.CLUBS),
  QUEEN_C(12, Suit.CLUBS),
  JACK_C(11, Suit.CLUBS),
  TEN_C(10, Suit.CLUBS),
  NINE_C(9, Suit.CLUBS),
  EIGHT_C(8, Suit.CLUBS),
  SEVEN_C(7, Suit.CLUBS),
  SIX_C(6, Suit.CLUBS),
  FIVE_C(5, Suit.CLUBS),
  FOUR_C(4, Suit.CLUBS),
  THREE_C(3, Suit.CLUBS),
  TWO_C(2, Suit.CLUBS),
  ACE_H(14, Suit.HEARTS),
  KING_H(13, Suit.HEARTS),
  QUEEN_H(12, Suit.HEARTS),
  JACK_H(11, Suit.HEARTS),
  TEN_H(10, Suit.HEARTS),
  NINE_H(9, Suit.HEARTS),
  EIGHT_H(8, Suit.HEARTS),
  SEVEN_H(7, Suit.HEARTS),
  SIX_H(6, Suit.HEARTS),
  FIVE_H(5, Suit.HEARTS),
  FOUR_H(4, Suit.HEARTS),
  THREE_H(3, Suit.HEARTS),
  TWO_H(2, Suit.HEARTS);

  public final int rank;
  public final Suit suit;

  Card(int rank, Suit suit){
    this.rank = rank;
    this.suit = suit;
  }

  private String face(){
    switch(rank){
      case 14: return "A";
      case 13: return "K";
      case 12: return "Q";
      case 11: return "J";
      case 10: return "T";
      default: return ""+rank;
    }
  }

  public String toString(){
    switch(suit){
      case HEARTS: return face()+"H";
      case CLUBS: return face()+"C";
      case DIAMONDS: return face()+"D";
      case SPADES: return face()+"S";
      default: return ""; 
    }
  }

}
