/**
	Test if the file can compile.
*/
public class t {
    public static void main(String[] args) {
        MossSideWhist msw = new MossSideWhist(new Agent21725083(), new RandomAgent(), new Agent21714084());
        msw.playGame(5, System.out);
	System.out.println(msw.getScores());
    }
}
