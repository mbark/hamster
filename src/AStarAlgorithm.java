import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the GameTree class which uses the 
 * A* algorithm to traverse the game tree.
 * @author Jonas Sköld
 */
public class AStarAlgorithm implements PathFindingAlgorithm {
	@Override public Solution findPathToGoal(GameState start) {
		Map<GameState, Integer> gScore  = new HashMap<>();
		Map<GameState, Integer> fScore = new HashMap<>();
		Set<GameState> closedSet = new HashSet<>();
		Set<GameState> openSet = new HashSet<>();
		openSet.add(start);
		Map<GameState, GameState> cameFrom = new HashMap<>();
		gScore.put(start, 0);
		fScore.put(start, estimatedTotalCost(start, gScore));
		
		while(!openSet.isEmpty()) {
			int lowestFScore = Integer.MAX_VALUE;
			GameState current = null;
			for(GameState state : openSet) {
				if(state.isDone()) {
					Solution sol = new Solution();
					reconstructPath(cameFrom, state, sol);
					return sol;
				}
				if(fScore.get(state) < lowestFScore) {
					lowestFScore = fScore.get(state);
					current = state;
				}
			}
			
			
			openSet.remove(current);
			closedSet.add(current);
			
			List<GameState> nextStates = current.getNextStates();
			for(GameState neighbor : nextStates) {
				int tentativeGScore = gScore.get(current) + 1;
				if(closedSet.contains(neighbor)) {
					if(tentativeGScore >= gScore.get(neighbor)) {
						continue;
					}
 				}
				
				if(!openSet.contains(neighbor) || tentativeGScore < gScore.get(neighbor)) {
					cameFrom.put(neighbor, current);
					gScore.put(neighbor, tentativeGScore);
					fScore.put(neighbor, estimatedTotalCost(neighbor, gScore));
					if(!openSet.contains(neighbor)) {
						openSet.add(neighbor);
					}
				}
			}
		}
		
		return null;
	}
	
	private void reconstructPath(Map<GameState, GameState> cameFrom, GameState current, Solution solution) {
		if(current.getLastMove() == null) {
			return;
		}
		solution.append(current.getLastMove().inverse());
		
		if(cameFrom.containsKey(current)) {
			GameState from = cameFrom.get(current);
			reconstructPath(cameFrom, from, solution);
		}
		
	}


	private int estimatedTotalCost(GameState currentState, Map<GameState, Integer> gScore) {
		return gScore.get(currentState) + estimatedCostToGoal(currentState);
	}

	private int estimatedCostToGoal(GameState currentState) {
		int distanceToGoal = currentState.getDistanceToGoal();
		return distanceToGoal;
	}
}
