import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Main {
	public static final void main(String[] args) throws IOException {
		List<String> boardStrings = read();
		GameState gs = GameState.calculateBoard(boardStrings);
	}
	
	public static List<String> read() throws IOException {
		List<String> boardStrings  = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String row;
		while((row = br.readLine()) != null) {
			boardStrings.add(row);
		}
		
		return boardStrings;
	}
}
