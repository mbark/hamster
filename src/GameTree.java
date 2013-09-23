
/**
 * The GameTree class is a representation of the possible moves
 * in a sokoban game.
 * 
 * @author Jonas Sk�ld
 */
public abstract class GameTree {
	private final GameState startState;
	private final GameState goalState;
	
	public GameTree(GameState startState, GameState goalState) {
		this.startState = startState;
		this.goalState = goalState;
	}
	
	/**
	 * Implementations of this method should find a path between
	 * the start state and the goal state - a {@link Solution}.
	 * @return a correct {@link Solution} from the start state 
	 * to the goal state.
	 */
	public abstract Solution findPathToGoal();
}
