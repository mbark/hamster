import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents a state of the game, with the current map and the positions of all
 * the moving objects in it.
 * <p>
 * Has a level with an existing board. A board could look like this:
 * 
 * <pre>
 * ########
 * #   # .#
 * #   $$.#
 * ####   #
 *    #@ ##
 *    ####
 * </pre>
 * 
 * With legend:
 * 
 * <pre>
 * ' ': free space
 * ’#’: wall
 * ’.’: goal
 * ’@’: Sokoban player
 * ’+’: Sokoban player on goal
 * ’$’: box
 * ’*’: box on goal
 * </pre>
 * 
 * The origin point (0,0) is defined in the top left corner of the map (walls
 * included).
 * <p>
 * If the map is of a non-rectangular shape, excessive space will be
 * "free space".
 * 
 */
public class GameState {

	public static final char FREE_SPACE = ' ';
	public static final char WALL = '#';
	public static final char GOAL = '$';
	public static final char PLAYER = '@';
	public static final char PLAYER_ON_BOX = '+';
	public static final char BOX = '.';
	public static final char BOX_ON_GOAL = '*';

	// this is practically a singleton. can be ignored in equals/hashCode
	//private final char[][] board;
	private final Board board;
	private final Deque<Move> movesToHere;
	
	private final Player player;
	private final Set<Box> boxes;
	
	GameState(Board board, Player player, Set<Box> boxes) {
		this (board, player, boxes, new LinkedList<Move>());
	}

	// internal constructor
	GameState(Board board, Player player, Set<Box> boxes, Move lastMove) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
		this.movesToHere = new LinkedList<>();
		movesToHere.addFirst(lastMove);
	}

	GameState(Board board, Player player, Set<Box> boxes, Deque<Move> movesToHere) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
		this.movesToHere = movesToHere;
	}

	public List<GameState> getNextBoxStates () {
		if (boxesAreDone())
			return runToGoalGameStates ();
		List<GameState> nextStates = new ArrayList<>();
		
		List<BoxMove> possibleBoxMoves = new ArrayList<>();
		for (Box box : boxes) {
			List<Move> possibleMoves = getPossibleMoves(box);
			for (Move move : possibleMoves)
				possibleBoxMoves.add (new BoxMove(box, move));
		}
		
		if (player == null)
			return createInitialStates (possibleBoxMoves);
		
		Map<BoxMove, Deque<Move>> movePaths = findMovePathsBFS (possibleBoxMoves);
		for (Entry<BoxMove, Deque<Move>> pathEntry : movePaths.entrySet()) {
			BoxMove boxMove = pathEntry.getKey();
			Deque<Move> moves = pathEntry.getValue();
			
			Player playerBeforeBoxMove = new Player(boxMove.box.getLocation().move(boxMove.move));
			Player movedPlayer = playerBeforeBoxMove.move(boxMove.move);
			Box movedBox = boxMove.box.move(boxMove.move);
			
			Set<Box> newBoxes = new HashSet<>(boxes);
			newBoxes.remove(boxMove.box);
			newBoxes.add(movedBox);
			moves.addLast (boxMove.move);
			GameState state = new GameState(board, movedPlayer, newBoxes, moves);
			nextStates.add (state);
		}
		return nextStates;
	}

	private List<GameState> createInitialStates(List<BoxMove> possibleBoxMoves) {
		List<GameState> initialStates = new ArrayList<>();
		for (BoxMove boxMove : possibleBoxMoves) {
			Player initialPlayer =
					new Player(boxMove.box.getLocation().move(boxMove.move));
			GameState initialState = new GameState(board, initialPlayer, boxes);
			initialStates.add(initialState);
		}
		return initialStates;
	}
	
	private List<GameState> runToGoalGameStates() {
		/*
		 * Re-use the BFS by fabricating a fake box, next to the end position of the player,
		 * that we want to push to the end position of the player
		 */
		Location playerEndLocation = board.getPlayerEndLocation ();
		Box dummyBox = new Box(playerEndLocation.move(Move.DOWN));
		Move dummyMove = Move.UP;
		BoxMove dummy = new BoxMove(dummyBox, dummyMove);
		List<BoxMove> dummyList = Collections.singletonList(dummy);
		Deque<Move> movesToEnd = findMovePathsBFS(dummyList).get(dummy);
		if(movesToEnd == null) // can't find path to "start" from here
			return Collections.emptyList();
		GameState endState =
				new GameState(board, new Player(playerEndLocation), boxes, movesToEnd);
		return Collections.singletonList(endState);
	}

	private List<Move> getPossibleMoves (Movable<?> m) {
		List<Move> possibleMoves = new ArrayList<>();
		Location oneUp = m.getLocation().move(Move.UP);
		Location twoUp = oneUp.move(Move.UP);
		Location oneRight = m.getLocation().move(Move.RIGHT);
		Location twoRight = oneRight.move(Move.RIGHT);
		Location oneDown = m.getLocation().move(Move.DOWN);
		Location twoDown = oneDown.move(Move.DOWN);
		Location oneLeft = m.getLocation().move(Move.LEFT);
		Location twoLeft = oneLeft.move(Move.LEFT);
		if (isFreeForPlayer(oneUp, twoUp))
			possibleMoves.add(Move.UP);
		if (isFreeForPlayer(oneRight, twoRight))
			possibleMoves.add(Move.RIGHT);
		if (isFreeForPlayer(oneDown, twoDown))
			possibleMoves.add(Move.DOWN);
		if (isFreeForPlayer(oneLeft, twoLeft))
			possibleMoves.add(Move.LEFT);
		return possibleMoves;
	}
	
	private Map<BoxMove, Deque<Move>> findMovePathsBFS(List<BoxMove> possibleBoxMoves) {
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
	
	/**
	 * Get all the significant {@link GameState} instances that are the result
	 * of one state change in the map.
	 * 
	 * @return {@link List} of {@link GameState} objects that are
	 *         "one step away"
	 */
	public List<GameState> getNextStates() {
		List<GameState> nextStates = new ArrayList<>();
		// ********** temporary shitty code to calculate next states ***********
		
		if(player == null) {
			for(Box box : boxes) {
				List<GameState> states = getStatesNextToBox(box);
				nextStates.addAll(states);
			}
			return nextStates;
		}

		nextStates.addAll (move(Move.UP));
		nextStates.addAll (move(Move.DOWN));
		nextStates.addAll (move(Move.LEFT));
		nextStates.addAll (move(Move.RIGHT));

		// *********************************************************************
		return nextStates;
	}
	
	private List<GameState> getStatesNextToBox(Box box) {
		List<GameState> states = new ArrayList<>(4);
		
		addIfFree(states, box, Move.UP);
		addIfFree(states, box, Move.DOWN);
		addIfFree(states, box, Move.LEFT);
		addIfFree(states, box, Move.RIGHT);
		
		return states;
	}
	
	private void addIfFree(List<GameState> states, Box box, Move move) {
		Location loc = box.getLocation().move(move);
		if(isFreeForPlayer(loc)) {
			Player player = new Player(loc);
			states.add(new GameState(board, player, boxes));
		}
	}

	// the List thing is a fulhack :( but it works :D maybe :/
	private List<GameState> move(Move move) {
		Player movedPlayer = player.move(move);
		if(!isFreeForPlayer(movedPlayer.getLocation())) {
			return Collections.emptyList();
		}
		
		for (Box box : boxes) {
			// can't move where there is a box
			if(box.getLocation().equals(movedPlayer.getLocation())) {
				return Collections.emptyList();
			}
			
			
			if(boxCanBePulled(box, player, move)) {
				// at this point, return two states
				// one with a moved box, and one with no moved boxes
				Box moved = box.move(move);
				Set<Box> movedBoxes = new HashSet<>(boxes);
				movedBoxes.remove(box);
				movedBoxes.add(moved);
				return Arrays.asList(new GameState(board, movedPlayer, movedBoxes, move),
									 new GameState(board, movedPlayer, boxes, move));
			}
		}

		// at this point, no boxes were moved
		return Arrays.asList (new GameState(board, movedPlayer, boxes, move));
	}
	
	// is true if all the locations are free
	// false if any of them isn't
	private boolean isFreeForPlayer (Location... locations) {
		for (Location loc : locations)
			if (!board.isFree(loc) || boxes.contains(new Box(loc)))
					return false;
		return true;
	}
	
	private boolean boxCanBePulled(Box box, Player player, Move move) {
		// if there is a box in the opposite direction of the move, return true
		Location inverseLocation = player.getLocation().move(move.inverse());
		return box.getLocation().equals(inverseLocation);
	}
	
	public boolean isDone() {
		/*
		 * initial state, player will be null and game won't be done
		 * 
		 * TODO: this won't work if the game is already solved form the beginning
		 */
		if (player == null)
			return false;
		Location loc = player.getLocation();
		char c = board.getCharForLocation(loc);
		if (c != PLAYER) {
			return false;
		}
		
		return boxesAreDone();
	}
	
	private boolean boxesAreDone () {
		for(Box box : boxes) {
			Location loc = box.getLocation();
			char c = board.getCharForLocation(loc);
			
			if(c != GOAL) {
				return false;
			}
		}
		return true;
	}
	
	public int getDistanceToGoal() {
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
	 * Create a {@link GameState} object as a sub-level of this GameState given
	 * a rectangular shape.
	 * 
	 * @param x
	 *            The x-coordinate that will mark the sub-Levels origin
	 * @param y
	 *            The y-coordinate that will mark the sub-Levels origin
	 * @param width
	 *            The width of the sub-Level
	 * @param height
	 *            The height of the sub-Level
	 * @return A {@link GameState} object with a sub-matrix of the original
	 *         Level
	 */
	public GameState subGameState(int x, int y, int width, int height) {
		Board subBoard = board.subBoard(x, y, width, height);
		return new GameState(subBoard, player, boxes);
	}

	public static GameState calculateBoard(List<String> boardStrings) {
		int height = boardStrings.size();
		int width = 0;
		for (String s : boardStrings)
			width = Math.max(width, s.length());
		char[][] board = new char[height][width];
		return fillBoard(board, boardStrings);
	}

	/**
	 * Get the {@link Move} instance that caused this {@link GameState}.
	 * 
	 * @return The final {@link Move} before this {@link GameState}
	 */
	public Deque<Move> getMovesToHere () {
		return movesToHere;
	}

	private static GameState fillBoard(char[][] board, List<String> boardStrings) {
		/*
		 * Fill the board using the board strings.
		 */
		Set<Box> boxes = new HashSet<Box>();
		Set<Goal> goals = new HashSet<Goal>();
		Box box;
		for (int row = 0; row < board.length; row++) {
			String rowString = boardStrings.get(row);
			for (int col = 0; col < board[row].length; col++) {
				char square = ' ';
				if (col < rowString.length())
					square = rowString.charAt(col);
				switch (square) {
				case FREE_SPACE:
				case WALL:
					board[row][col] = square;
					break;
				case PLAYER_ON_BOX:
					box = new Box(new Location(col, row));
					boxes.add(box);
				case PLAYER:
					board[row][col] = PLAYER;
					break;
				case BOX_ON_GOAL:
					box = new Box(new Location(col, row));
					boxes.add(box);
				case GOAL:
					board[row][col] = GOAL;
					goals.add(new Goal(new Location(col, row)));
					break;
				case BOX:
					board[row][col] = FREE_SPACE;
					box = new Box(new Location(col, row));
					boxes.add(box);
					break;
				}
			}
		}
		return new GameState(new Board(board, goals), null, boxes);
	}
	
	public int hashWithoutMoves () {
		int hashCode = 0;
		if(player != null) {
			hashCode += player.hashCode();
		}
		return hashCode + 31*boxes.hashCode();
	}
	
	@Override public int hashCode() {
		int hashCode = 0;
		if(player != null) {
			hashCode += player.hashCode();
		}
		return hashCode + 31*boxes.hashCode() + movesToHere.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof GameState))
			return false;
		GameState g = (GameState) obj;
		return player.equals(g.player) &&
				boxes.equals(g.boxes) &&
				movesToHere.equals(g.movesToHere);
	}
	
	@Override public String toString() {
		char[][] matrix = board.getBoardMatrix ();
		for (Box box : boxes) {
			Location l = box.getLocation();
			char c = board.getCharForLocation(l);
			if (c == GOAL)
				matrix[l.getRow()][l.getCol()] = BOX_ON_GOAL;
			else
				matrix[l.getRow()][l.getCol()] = BOX;
		}
		if (player != null) {
			Location pl = player.getLocation();
			matrix[pl.getRow()][pl.getCol()] = PLAYER;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < matrix.length; row++) {
			for (int col = 0; col < matrix[row].length; col++) {
				sb.append(matrix[row][col]);
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	/**
	 * Simple class used to keep track of the move performed on a box.
	 */
	static class BoxMove {
		final Box box;
		final Move move;
		
		public BoxMove(Box box, Move move) {
			this.box = box;
			this.move = move;
		}
		
		@Override public String toString() {
			return box.toString() + " " + move.toString();
		}
		
		@Override public int hashCode() {
			return box.hashCode();// + move.hashCode();
		}
		
		@Override public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof BoxMove))
				return false;
			BoxMove bm = (BoxMove) o;
			return box.equals(bm.box) && move.equals(bm.move);
		}
	}
}
