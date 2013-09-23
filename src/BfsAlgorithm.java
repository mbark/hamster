import java.util.LinkedList;
import java.util.Queue;


public class BfsAlgorithm implements PathFindingAlgorithm {

	@Override public Solution findPathToGoal(GameState startState) {
		Queue<GameState> queue = new LinkedList<>();
		queue.add(startState);
		return null;
	}
}
