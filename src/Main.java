import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Main {
	
	private static final PathFindingAlgorithm BFS = new BfsAlgorithm();
	private static final PathFindingAlgorithm A_STAR = new AStarAlgorithm();
	
	public static final void main(String[] args) throws IOException {
		List<String> boardStrings = read();
		GameState gs = GameState.calculateBoard(boardStrings);
		Solution solution = BFS.findPathToGoal(gs);
		System.out.println(solution);
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
