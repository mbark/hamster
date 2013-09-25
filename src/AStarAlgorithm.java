import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * An implementation of the GameTree class which uses the 
 * A* algorithm to traverse the game tree.
 * @author Jonas Sköld
 */
public class AStarAlgorithm implements PathFindingAlgorithm {
	Map<Node, Integer> gScore  = new HashMap<>();
	Map<Node, Integer> fScore = new HashMap<>();
	
	@Override public Solution findPathToGoal(GameState startState) {
		Set<Node> closedSet = new HashSet<>();
		TreeSet<Node> openSet = new TreeSet<>();
		Set<Node> visitedNodes = new HashSet<>();
		Map<GameState, GameState> cameFrom = new HashMap<>();
		
		Node start = new Node(startState);
		gScore.put(start, 0);
		fScore.put(start, estimatedTotalCost(start, gScore));
		openSet.add(start);
		
		while(!openSet.isEmpty()) {
			Node current = openSet.pollFirst();
			if(current.gameState.isDone()) {
				Solution solution = new Solution();
				reconstructPath(cameFrom, current.gameState, solution);
				return solution;
			}
			
			openSet.remove(current);
			closedSet.add(current);
			visitedNodes.add(current);
			
			List<GameState> nextStates = current.gameState.getNextStates();
			for(GameState neighborState : nextStates) {
				Node neighbor = new Node(neighborState);
				int tentativeGScore = gScore.get(current) + 1;
				if(closedSet.contains(neighbor)) {
					if(tentativeGScore >= gScore.get(neighbor)) {
						continue;
					}
 				}
				
				if(!openSet.contains(neighbor) || tentativeGScore < gScore.get(neighbor)) {
					cameFrom.put(neighbor.gameState, current.gameState);
					gScore.put(neighbor, tentativeGScore);
					fScore.put(neighbor, estimatedTotalCost(neighbor, gScore));
					openSet.add(neighbor);
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

	private int estimatedTotalCost(Node currentState, Map<Node, Integer> gScore) {
		return gScore.get(currentState) + estimatedCostToGoal(currentState);
	}

	private int estimatedCostToGoal(Node currentState) {
		int distanceToGoal = currentState.gameState.getDistanceToGoal();
		return 4 * distanceToGoal;
	}
	
	private final class Node implements Comparable<Node> {
		GameState gameState;
		
		private Node(GameState gameState) {
			this.gameState = gameState;
		}

		@Override
		public int compareTo(Node node) {
			Integer myScore = fScore.get(this);
			if(myScore == null) {
				myScore = estimatedTotalCost(node, gScore);
				fScore.put(this, myScore);
			}
			Integer otherScore = fScore.get(node);
			if(otherScore == null) {
				otherScore = estimatedTotalCost(node, gScore);
				fScore.put(node, otherScore);
			}
			int score = myScore - otherScore;
			if(score == 0) {
				score = 1;
			}
			
			return score;
		}
		
		@Override
		public int hashCode() {
			return gameState.hashCode();
		}
		

		@Override public boolean equals(Object obj) {
			if(!(obj instanceof Node)) {
				return false;
			}
			Node other = (Node) obj;
			return gameState.equals(other.gameState);
		}
		
	}
}
