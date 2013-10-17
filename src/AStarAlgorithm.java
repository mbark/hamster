import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of the GameTree class which uses the 
 * A* algorithm to traverse the game tree.
 */
public class AStarAlgorithm implements PathFindingAlgorithm {

	Map<GameState, Integer> gScore  = new HashMap<>();
	Map<GameState, Integer> fScore = new HashMap<>();
	
	// concurrent fields for thread synchronization
	private final ConcurrentMap<BoxOnlyGameState, GameState> visited; 
	private final ConcurrentMap<BoxOnlyGameState, GameState> otherThreadVisited; 
	private final CountDownLatch latch;
	private final AtomicReference<BoxOnlyGameState> meetingPoint;
	private final Map<GameState, GameState> cameFrom = new HashMap<>();

	public AStarAlgorithm(ConcurrentMap<BoxOnlyGameState, GameState> visited, 
			ConcurrentMap<BoxOnlyGameState, GameState> otherThreadVisited, CountDownLatch latch,
			 AtomicReference<BoxOnlyGameState> meetingPoint) {
		this.visited = visited;
		this.otherThreadVisited = otherThreadVisited;
		this.latch = latch;
		this.meetingPoint = meetingPoint;
	}

	public Solution findPathToGoal(GameState start) {
		Set<GameState> visitedNodes = new HashSet<>();
		Set<GameState> closedSet = new HashSet<>();
		TreeSet<GameState> openSet = new TreeSet<>(getComparator());

		gScore.put(start, 0);
		fScore.put(start, estimatedTotalCost(start, gScore));
		openSet.add(start);

		while(!openSet.isEmpty()) {
			GameState current = openSet.pollFirst();
			if(current.isDone())
				return reconstructPath(cameFrom, current);

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
	
	@Override public void findPathToGoal(GameState start, CyclicBarrier barrier) throws InterruptedException, BrokenBarrierException {
		Set<GameState> visitedNodes = new HashSet<>();
		Set<GameState> closedSet = new HashSet<>();
		TreeSet<GameState> openSet = new TreeSet<>(getComparator());

		gScore.put(start, 0);
		fScore.put(start, estimatedTotalCost(start, gScore));
		openSet.add(start);
		visited.put(new BoxOnlyGameState(start), start);
		barrier.await();

		while(!openSet.isEmpty()) {
			GameState current = openSet.pollFirst();
			visited.put(new BoxOnlyGameState(current), current);
			if (checkMeetingPoint(cameFrom) || tryToFindMeetingPoint(cameFrom, current)) {
				//mark as done
				latch.countDown();
				return;
			}
			
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
		latch.countDown();
		return;
	}
	
	private boolean checkMeetingPoint (Map<GameState, GameState> cameFrom) {
		//Check if other thread has found meeting point
		BoxOnlyGameState rendezVouz = meetingPoint.get();
		return rendezVouz != null;
	}
	
	private boolean tryToFindMeetingPoint (Map<GameState, GameState> cameFrom, GameState current) {
		//Check if we have found meeting point
		BoxOnlyGameState boxOnly = new BoxOnlyGameState(current);
		GameState match = otherThreadVisited.get(boxOnly);
		if (match != null) {
			//If so, check if I can go to the player position of that gamestate
			Location l = match.getPlayerLocation();
			GameState linkingGameState = current.getPlayerMoveGameState(l);
			if (linkingGameState != null) {
				//If so, try to set meeting point
				if (setMeetingPoint(boxOnly)) {
					if (!linkingGameState.equals(current)) {
						cameFrom.put(linkingGameState, current);
						visited.put(new BoxOnlyGameState(linkingGameState), linkingGameState);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean setMeetingPoint(BoxOnlyGameState boxOnly) {
		synchronized (meetingPoint) {
			if (meetingPoint.get() == null) {
				meetingPoint.set(boxOnly);
				return true;
			}
			return false;
		}
	}

	private Solution reconstructPath(Map<GameState, GameState> cameFrom, GameState endState) {
		GameState state = endState;
		Solution solution = (endState instanceof BackwardsGameState) 
			? new BackwardSolution()
			: new ForwardSolution();
		while (state != null) {
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
					score = 1;
				}
				
				return score;
			}
		};
	}
	
	public Solution getSolution() {
		BoxOnlyGameState rendezVouz = meetingPoint.get();
		GameState linkingGameState = visited.get(rendezVouz);
		return reconstructPath(cameFrom, linkingGameState);
	}
}
