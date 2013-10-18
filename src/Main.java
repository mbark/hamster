import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Main {
	
	
	public static final void main2(String[] args) throws IOException {
		List<String> boardStrings = read();
		GameState gs = ForwardsGameState.calculateBoard(boardStrings);
		AStarAlgorithm aStar = new AStarAlgorithm(gs);
		while(!aStar.nextStep());
		Solution solution = aStar.getSolution();
		System.out.println(solution);
	}
	
	public static final void main(String... args) throws IOException {
		List<String> boardStrings = read();
		final GameState start = ForwardsGameState.calculateBoard(boardStrings);
		final GameState goal = BackwardsGameState.calculateBoard(boardStrings);
		
		AStarAlgorithm aStarForward = new AStarAlgorithm(start);
		AStarAlgorithm aStarBackward = new AStarAlgorithm(goal);
		aStarBackward.setOtherAStar(aStarForward);
		aStarForward.setOtherAStar(aStarBackward);
		
		boolean isDone = false;
		while(!isDone) {
			isDone = isDone || aStarBackward.nextStep();
			isDone = isDone || aStarBackward.nextStep();
			isDone = isDone || aStarForward.nextStep();
		}

		Solution forwardSolution = aStarForward.getSolution();
		Solution backwardSolution = aStarBackward.getSolution().getForwardSolution();
		
		System.out.println(forwardSolution.toString() + " " + backwardSolution.toString());
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
