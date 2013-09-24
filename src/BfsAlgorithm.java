import java.util.*;



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
		while (state.getLastMove() != null) {
			solution.append(state.getLastMove().inverse());
			state = visited.get(state);
		}
		
		return solution;
	}
}
