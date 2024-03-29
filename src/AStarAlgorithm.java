import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * An implementation of the GameTree class which uses the 
 * A* algorithm to traverse the game tree.
 */
public class AStarAlgorithm {

	private static final int GOAL_DISTANCE_WEIGHT = 10;
	private static final int MOVE_WEIGHT = 0;
	private static final int OBSTACLE_WEIGHT = 0;
	
	private static final int STATE_DIFFERENCE_WEIGHT = 0;
	
	final Map<GameState, GameState> cameFrom = new HashMap<>();
	final Map<GameState, Integer> gScore  = new HashMap<>();
	final Map<GameState, Integer> fScore = new HashMap<>();
	final Set<GameState> visitedNodes = new HashSet<>();
	final Set<GameState> closedSet = new HashSet<>();
	final TreeSet<GameState> openSet = new TreeSet<>(getComparator());
	final GameState start;
	GameState current;
	
	private boolean isDone = false;
	GameState rendevouz = null;
	private AStarAlgorithm otherAStar;
	

	public AStarAlgorithm(GameState start) {
		this.start = start;
		gScore.put(start, 0);
		fScore.put(start, estimatedTotalCost(start, gScore));
		openSet.add(start);
		cameFrom.put(start, null);
		current = start;
	}
	
	public void setOtherAStar(AStarAlgorithm aStar) {
		otherAStar = aStar;
	}
	
	public boolean nextStep() {
		if(openSet.isEmpty() || isDone) {
			return true;
		}
		
		current = openSet.pollFirst();
		if(canFinish(current))
			return true;
		closedSet.add(current);
		visitedNodes.add(current);
		
		List<GameState> goalMacro = current.tryGoalMacro();
		if (goalMacro != null && !goalMacro.isEmpty()) {
			GameState previousState = current;
			int cost = 1;
			for (GameState state : goalMacro) {
				visitedNodes.add(state);
				closedSet.add(state);
				cameFrom.put(state, previousState);
				gScore.put(state, gScore.get(current) + cost);
				fScore.put(state, estimatedTotalCost(state, gScore));
				previousState = state;
				cost++;
			}
			current = previousState;
		}
		
		if (canFinish(current))
			return true;
		
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
		
		return false;
	}
	
	private boolean canFinish(GameState current) {
		if(current.isDone() || hasReachedRendevouz(current)) {
			isDone = true;
			rendevouz = current;
			setRendevouzForOther(current);
			return true;
		}
		return false;
	}
	
	private boolean hasReachedRendevouz(GameState current) {
		if(otherAStar == null) {
			return false;
		} else {
			return otherAStar.contains(current);
		}
	}
	
	private void setRendevouzForOther(GameState rendevouz) {
		if(otherAStar != null) {
			GameState otherRendevouz = otherAStar.findCorrespondingKey(rendevouz);
			if (otherRendevouz.getPlayerLocation() != null) {
				Deque<Move> movesToLink =
						rendevouz.getPlayerMoveGameState(otherRendevouz.getPlayerLocation()).getMovesToHere();
				rendevouz.getMovesToHere().addAll(movesToLink);
			}
			otherAStar.rendevouz = otherRendevouz;
			otherAStar.isDone = true;
		}
	}
	
	private boolean contains(GameState state) {
		return cameFrom.containsKey(state);
	}
	
	private GameState findCorrespondingKey (GameState key) {
		for (GameState corrKey : cameFrom.keySet())
			if (corrKey.equals(key))
				return corrKey;
		for (GameState corrKey : cameFrom.keySet())
			if (corrKey.getBoxes().equals(key.getBoxes()))
				return corrKey;
		return null;
	}
	
	public Solution getSolution() {
		return reconstructPath(cameFrom, rendevouz);
	}
	
	private Solution reconstructPath(Map<GameState, GameState> cameFrom, GameState endState) {
		Solution solution = (start instanceof BackwardsGameState) 
			? new BackwardSolution()
			: new ForwardSolution();
		GameState state = endState;
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
		int cost = currentState.getDistanceToGoal() * GOAL_DISTANCE_WEIGHT;
		cost += currentState.getMovesToHere().size() * MOVE_WEIGHT;
		cost += currentState.numObstacles() * OBSTACLE_WEIGHT;
		cost += (otherAStar == null ? 0 :
			currentState.difference(otherAStar.current)) * STATE_DIFFERENCE_WEIGHT;
		return cost;
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
}
