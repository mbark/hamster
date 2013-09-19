import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GameState {
	private ArrayList<ArrayList<Character>> board;
	
	public static void main(String[] args) {
		try {
			GameState state = new GameState();
			state.read();
			state.print();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public GameState() {
		board = new ArrayList<ArrayList<Character>>();
	}
	
	public void read() throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(System.in));
		
		String line;
		while((line = br.readLine()) != null) {
			ArrayList<Character> list = new ArrayList<Character>();
			char[] c = line.toCharArray();
			for(int i = 0; i<c.length; i++) {
				list.add(c[i]);
			}
			board.add(list);
		}
	}
	
	public void print() {
		for(int i = 0; i<board.size(); i++) {
			for(int j = 0; j<board.get(i).size(); j++) {
				char c = board.get(i).get(j);
				System.out.print(c);
			}
			System.out.println();
		}
	}
}
