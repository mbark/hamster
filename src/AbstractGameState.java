import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public abstract class AbstractGameState implements GameState {
	
	// this is practically a singleton. can be ignored in equals/hashCode
	protected final Board board;
	protected final Deque<Move> movesToHere;
	
	protected final Player player;
	protected final Set<Box> boxes;

	AbstractGameState (Board board, Player player, Set<Box> boxes) {
		this (board, player, boxes, new LinkedList<Move>());
	}

	AbstractGameState(Board board, Player player, Set<Box> boxes, Move lastMove) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
		this.movesToHere = new LinkedList<>();
		movesToHere.addFirst(lastMove);
	}

	AbstractGameState(Board board, Player player, Set<Box> boxes, Deque<Move> movesToHere) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
		this.movesToHere = movesToHere;
	}

	/**
	 * Get the {@link Move} instance that caused this {@link BackwardsGameState}.
	 * 
	 * @return The final {@link Move} before this {@link BackwardsGameState}
	 */
	@Override public Deque<Move> getMovesToHere () {
		return movesToHere;
	}
	
	@Override public Set<Box> getBoxes() {
		return boxes;
	}
	
	@Override public Location getPlayerLocation() {
		return player.getLocation();
	}
	
	@Override public GameState getPlayerMoveGameState(Location l) {
		/*
		 * Re-use the BFS by fabricating a fake box, next the Location l,
		 * that we want to pull up to the Location l
		 */
		Box dummyBox = new Box(l.move(Move.DOWN));
		Move dummyMove = Move.UP;
		BoxMove dummy = new BoxMove(dummyBox, dummyMove);
		List<BoxMove> dummyList = Collections.singletonList(dummy);
		Deque<Move> movesToEnd = findBackwardsMovePathsBFS(dummyList).get(dummy);
		if(movesToEnd == null) // can't find path to "start" from here
			return null;
		if (this instanceof ForwardsGameState) {
			return new ForwardsGameState(board, new Player(l), boxes, movesToEnd);
		} else {
			return new BackwardsGameState(board, new Player(l), boxes, movesToEnd);
		}
	}
	
	@Override public int getDistanceToGoal() {
		int totalDistance = 0;
		
		Set<Goal> goals = board.getGoals();
		for(Goal goal : goals) {
			int shortestDistance = Integer.MAX_VALUE;
			for(Box box : boxes) {
				int distance = Location.distance(goal.getLocation(), box.getLocation());
				if(distance < shortestDistance) {
					shortestDistance = distance;
				}
			}
			totalDistance += shortestDistance;
		}
		
		return totalDistance;
	}
	
	/**
	 * Examines whether all of the given {@link Location}'s are free from both
	 * walls and boxes.
	 * @param locations the Locations to examine
	 * @return <code>true</code> if all the locations are free, <code>false</code> if any of them isn't
	 */
	protected boolean isFreeForPlayer (Location... locations) {
		for (Location loc : locations)
			if (!board.isFree(loc) || boxes.contains(new Box(loc)))
				return false;
		return true;
	}
	
	protected Map<BoxMove, Deque<Move>> findBackwardsMovePathsBFS (List<BoxMove> possibleBoxMoves) {
		Set<Location> possibleLocations = new HashSet<>();
		for (BoxMove boxMove : possibleBoxMoves)
			possibleLocations.add(boxMove.box.getLocation().move(boxMove.move));
		Queue<Location> queue = new LinkedList<>();
		queue.add(player.getLocation());
		Map<Location, Move> visited = new HashMap<>();
		visited.put(player.getLocation(), null);
		
		while (!queue.isEmpty()) {
			Location location = queue.poll();
			if (possibleLocations.isEmpty())
				break;
			possibleLocations.remove(location);
			
			for (Move move : Move.values()) {
				Location newLocation = location.move(move);
				if (visited.containsKey (newLocation) || !isFreeForPlayer(newLocation))
					continue;
				visited.put(newLocation, move);
				queue.add(newLocation);
			}
		}
		
		//Reconstruct all paths
		Map<BoxMove, Deque<Move>> pathsToPossibleBoxMoves = new HashMap<>();
		for (BoxMove boxMove : possibleBoxMoves) {
			Location currentLocation = boxMove.box.getLocation().move(boxMove.move);
			if (!visited.containsKey(currentLocation)) // unreachable boxMove
				continue;
			// if we already are in the right location for this boxmove
			// return empty list of moves
			if (currentLocation.equals(player.getLocation())) {
				pathsToPossibleBoxMoves.put(boxMove, new LinkedList<Move>());
				continue;
			}
			Deque<Move> path = new LinkedList<>();
			while (!currentLocation.equals(player.getLocation())) {
				Move move = visited.get(currentLocation);
				path.addFirst (move);
				currentLocation = currentLocation.move(move.inverse());
			}
			pathsToPossibleBoxMoves.put(boxMove, path);
		}
		return pathsToPossibleBoxMoves;
	}
	
	@Override public int hashCode() {
		int hashCode = 0;
		if(player != null) {
			hashCode += player.hashCode();
		}
		return hashCode + 31*boxes.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof AbstractGameState))
			return false;
		AbstractGameState g = (AbstractGameState) obj;
		if(player == null) {
			if(g.player != null) {
				return false;
			}
		}
		return player.equals(g.player) &&
				boxes.equals(g.boxes);
	}
}
