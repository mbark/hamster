import java.util.Map;

/**
 * An implementation of the GameTree class which uses the 
 * A* algorithm to traverse the game tree.
 * @author Jonas Sköld
 */
public class AStarAlgorithm implements PathFindingAlgorithm {
	private Map<GameState, Integer> costFromStart;

	@Override public Solution findPathToGoal(GameState startState) {
		//TODO Implement with A*
		return null;
	}
	
	private int estimatedTotalCost(GameState currentState) {
		return costFromStart.get(currentState) + estimatedCostToGoal(currentState);
	}
	
	private int estimatedCostToGoal(GameState currentState) {
		//return currentState.distanceToGoalState();
		return 0;
	}
}
