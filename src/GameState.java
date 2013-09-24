import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
	private final Move lastMove;
	
	private final Player player;
	private final Set<Box> boxes;

	// internal constructor
	GameState(Board board, Player player, Set<Box> boxes, Move lastMove) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
		this.lastMove = lastMove;
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
			states.add(new GameState(board, player, boxes, null));
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
	
	private boolean isFreeForPlayer (Location loc) {
		return board.isFree(loc) && !boxes.contains(new Box(loc));
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
		
		for(Box box : boxes) {
			loc = box.getLocation();
			c = board.getCharForLocation(loc);
			
			if(c != GOAL) {
				return false;
			}
		}
		
		
		return true;
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
		return new GameState(subBoard, player, boxes, null);
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
	public Move getLastMove() {
		return lastMove;
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
		return new GameState(new Board(board, goals), null, boxes, null);
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
		if (!(obj instanceof GameState))
			return false;
		GameState g = (GameState) obj;
		return player.equals(g.player) && boxes.equals(g.boxes);
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
		Location pl = player.getLocation();
		matrix[pl.getRow()][pl.getCol()] = PLAYER;
		
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < matrix.length; row++) {
			for (int col = 0; col < matrix[row].length; col++) {
				sb.append(matrix[row][col]);
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
