import java.util.Map;

/**
 * An implementation of the GameTree class which uses the 
 * A* algorithm to traverse the game tree.
 * @author Jonas Sköld
 */
public class GameTreeAStar extends GameTree {
	private Map<GameState, Integer> costFromStart;

	public GameTreeAStar(GameState startState, GameState goalState) {
		super(startState, goalState);
	}
	
	@Override public Solution findPathToGoal() {
		//TODO Implement with A*
		return null;
	}
	
	private int estimatedTotalCost(GameState currentState) {
		return costFromStart.get(currentState) + estimatedCostToGoal(currentState);
	}
	
	private int estimatedCostToGoal(GameState currentState) {
		return 0;//TODO
	}
}
