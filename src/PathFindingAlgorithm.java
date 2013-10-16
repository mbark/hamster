import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


/**
 * The GameTree interface describes an implementation that can solve
 * an input game.
 * 
 * @author Jonas Sköld
 */
public interface PathFindingAlgorithm {
	
	/**
	 * Implementations of this method should find a path between
	 * the start state and a goal state - a {@link Solution}.
	 * @return a correct {@link Solution} from the start state 
	 * to a goal state.
	 */
	Solution findPathToGoal(GameState startState);
	void findPathToGoal(GameState startState, CyclicBarrier barrier) throws InterruptedException, BrokenBarrierException;
	Solution getSolution();
}
