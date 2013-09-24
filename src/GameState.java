import java.util.ArrayList;
import java.util.Arrays;
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

		GameState up = move(Move.UP);
		if(up != null) {
			nextStates.add(up);
		}
		GameState down = move(Move.DOWN);
		if(down != null) {
			nextStates.add(down);
		}
		GameState left = move(Move.LEFT);
		if(left != null) {
			nextStates.add(left);
		}
		GameState right = move(Move.RIGHT);
		if(right != null) {
			nextStates.add(right);
		}
		

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
		if(board.isFree(loc)) {
			Player player = new Player(loc);
			states.add(new GameState(board, player, boxes, null));
		}
	}

	private GameState move(Move move) {
		Player pl = player.move(move);
		if(!board.isFree(pl.getLocation())) {
			return null;
		}
		
		Set<Box> boxesMoved = new HashSet<>();
		for (Box box : boxes) {
			if(box.getLocation().equals(pl.getLocation())) {
				return null;
			}
			
			if(boxWillBePulled(box, pl, move)) {
				Box moved = box.move(move);
				boxesMoved.add(moved);
			} else {
				boxesMoved.add(box);
			}
		}

		return new GameState(board, pl, boxesMoved, move);
	}
	
	private boolean boxWillBePulled(Box box, Player player, Move move) {
		Location inverseMove = player.getLocation().move(move.inverse());
		return box.getLocation().equals(inverseMove);
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
				char square = rowString.charAt(col);
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
		return player.hashCode() + 31*boxes.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof GameState))
			return false;
		GameState g = (GameState) obj;
		return player.equals(g.player) && boxes.equals(g.boxes);
	}

	public Move getLastMove() {
		return lastMove;
	}
}
