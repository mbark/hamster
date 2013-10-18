import java.util.Comparator;
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

	final Map<GameState, GameState> cameFrom = new HashMap<>();
	final Map<GameState, Integer> gScore  = new HashMap<>();
	final Map<GameState, Integer> fScore = new HashMap<>();
	final Set<GameState> visitedNodes = new HashSet<>();
	final Set<GameState> closedSet = new HashSet<>();
	final TreeSet<GameState> openSet = new TreeSet<>(getComparator());
	
	private boolean isDone = false;
	GameState rendevouz = null;
	private AStarAlgorithm otherAStar;
	

	public AStarAlgorithm(GameState start) {
		gScore.put(start, 0);
		fScore.put(start, estimatedTotalCost(start, gScore));
		openSet.add(start);
	}
	
	public void setOtherAStar(AStarAlgorithm aStar) {
		otherAStar = aStar;
	}
	
	public boolean nextStep() {
		if(openSet.isEmpty() || isDone) {
			return true;
		}
		
		GameState current = openSet.pollFirst();
		if(current.isDone() || hasReachedRendevouz(current)) {
			isDone = true;
			rendevouz = current;
			setRendevouzForOther(current);
			return true;
		}

		closedSet.add(current);
		visitedNodes.add(current);
		
		List<GameState> goalMacro = current.tryGoalMacro();
		if (goalMacro != null) {
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

	public boolean isDone() {
		return isDone;
	}
	
	public boolean hasReachedRendevouz(GameState current) {
		if(otherAStar == null) {
			return false;
		} else {
			return otherAStar.contains(current);
		}
	}
	
	public void setRendevouzForOther(GameState rendevouz) {
		if(otherAStar != null) {
//			TODO: this must be done properly
			otherAStar.rendevouz = rendevouz;
//			otherAStar.rendevouz = otherAStar.cameFrom.get(rendevouz);
			otherAStar.isDone = true;
		}
	}
	
	public boolean contains(GameState state) {
		return cameFrom.containsKey(state);
	}
	
	public GameState get(BoxOnlyGameState state) {
		return cameFrom.get(state);
	}
	
	public Solution getSolution() {
		return reconstructPath(cameFrom, rendevouz);
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
}
