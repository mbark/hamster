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
			
			for (GameState nextState : state.getNextStates()) {
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
		Solution solution = new Solution();
		while (!state.getMovesToHere().isEmpty()) {
			for (Move move : state.getMovesToHere())
				solution.append(move.inverse());
			state = visited.get(state);
		}
		
		return solution;
	}
}
