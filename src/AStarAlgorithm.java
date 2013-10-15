import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of the GameTree class which uses the 
 * A* algorithm to traverse the game tree.
 */
public class AStarAlgorithm implements PathFindingAlgorithm {

	Map<GameState, Integer> gScore  = new HashMap<>();
	Map<GameState, Integer> fScore = new HashMap<>();
	
	// concurrent fields for thread synchronization
	private final ConcurrentMap<BoxOnlyGameState, GameState> visited; // will be thread safe
	private final CountDownLatch latch;
	private final AtomicReference<BoxOnlyGameState> meetingPoint;

	public AStarAlgorithm(ConcurrentMap<BoxOnlyGameState, GameState> visited, CountDownLatch latch,
			 AtomicReference<BoxOnlyGameState> meetingPoint) {
		this.visited = visited;
		this.latch = latch;
		this.meetingPoint = meetingPoint;
	}

	@Override public Solution findPathToGoal(GameState start) {
		Set<GameState> visitedNodes = new HashSet<>();
		Set<GameState> closedSet = new HashSet<>();
		TreeSet<GameState> openSet = new TreeSet<>(getComparator());
		Map<GameState, GameState> cameFrom = new GameStateMap();

		gScore.put(start, 0);
		fScore.put(start, estimatedTotalCost(start, gScore));
		openSet.add(start);

		while(!openSet.isEmpty()) {
			GameState current = openSet.pollFirst();
			if(current.isDone())
				return reconstructPath(cameFrom, current);

			openSet.remove(current);
			closedSet.add(current);
			visitedNodes.add(current);

			List<GameState> nextStates = current.getNextBoxStates();
			for(GameState neighbor : nextStates) {
				if(visitedNodes.contains(neighbor)) {
					continue;
				}
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
						visitedNodes.add(neighbor);
					}
				}
			}
		}

		return null;
	}

	private Solution reconstructPath(Map<GameState, GameState> cameFrom, GameState endState) {
		GameState state = endState;
		Solution solution = (endState instanceof BackwardsGameState) 
			? new BackwardSolution()
			: new ForwardSolution();
		while (state != null && !state.getMovesToHere().isEmpty()) {
			solution.prepend(state.getMovesToHere());
			state = cameFrom.get(state);
		}
		return solution;
	}

	private int estimatedTotalCost(GameState currentState, Map<GameState, Integer> gScore) {
		return gScore.get(currentState) + estimatedCostToGoal(currentState);
	}

	private int estimatedCostToGoal(GameState currentState) {
		int distanceToGoal = currentState.getDistanceToGoal();
		int nrOfMoves = currentState.getMovesToHere().size();
		return 40 * distanceToGoal + 5 * nrOfMoves;
	}
	
	private Comparator<GameState> getComparator() {
		return new Comparator<GameState>() {
			@Override
			public int compare(GameState state1, GameState state2) {
				if(state1.equals(state2)) {
					return 0;
				} else if(!gScore.containsKey(state1)) {
					return Integer.MAX_VALUE;
				}
				Integer myScore = fScore.get(state1);
				if(myScore == null) {
					myScore = estimatedTotalCost(state1, gScore);
					fScore.put(state1, myScore);
				}
				Integer otherScore = fScore.get(state2);
				if(otherScore == null) {
					otherScore = estimatedTotalCost(state2, gScore);
					fScore.put(state2, otherScore);
				}
				int score = myScore - otherScore;
				if(score == 0) {
					score = Math.random() > 0.5 ? 1 : -1;
				}
				
				return score;
			}
		};
	}
	
	
}
