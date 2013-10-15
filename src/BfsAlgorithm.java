import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;



public class BfsAlgorithm implements PathFindingAlgorithm {

	@Override public Solution findPathToGoal(GameState startState) {
		Queue<GameState> queue = new LinkedList<>();
		queue.add(startState);
		Map<GameState, GameState> visited = new HashMap<>(); 
		while (!queue.isEmpty()) {
			GameState state = queue.poll();
			if (state.isDone())
				return createSolution (state, startState, visited);
			
			for (GameState nextState : state.getNextBoxStates()) {
				if (visited.containsKey (nextState))
					continue;
				visited.put (nextState, state);
				queue.add(nextState);
			}
		}
		return null;
	}

	private Solution createSolution(GameState state, GameState startState,
									Map<GameState, GameState> visited) {
		Solution solution = new BackwardSolution();
		while (!state.getMovesToHere().isEmpty()) {
			solution.prepend(state.getMovesToHere());
			state = visited.get(state);
		}
		
		return solution;
	}
	
	@Override
	public Solution getSolution() {
		// TODO Auto-generated method stub
		return null;
	}
}
