import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	public static final char GOAL = '.';
	public static final char PLAYER = '@';
	public static final char PLAYER_ON_GOAL = '+';
	public static final char BOX = '$';
	public static final char BOX_ON_GOAL = '*';

	private final char[][] board;
	private final Player player;
	private final List<Box> boxes;

	// internal constructor
	GameState(char[][] board, Player player, List<Box> boxes) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
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

	private GameState move(Move move) {
		Player pl = player.move(move);
		if(!isFree(pl.getLocation())) {
			return null;
		}
		
		List<Box> boxesMoved = new ArrayList<>();
		for (int i = 0; i < boxes.size(); i++) {
			Box b = boxes.get(i);
			if(!isOnSameLocation(pl, b)) {
				boxesMoved.add(b);
				continue;
			}
			
			Box moved = b.move(move);
			if(!isFree(moved.getLocation())) {
				return null;
			} else {
				for(int j = 0; j<boxes.size(); j++) {
					if(i == j) {
						continue;
					}
					if(moved.getLocation().equals(b.getLocation())) {
						return null;
					}
				}
				boxesMoved.add(moved);
			}
		}

		return new GameState(board, pl, boxesMoved);
	}
	
	private boolean isOnSameLocation(Player player, Box box) {
		return player.getLocation().equals(box.getLocation());
	}
	
	private boolean isFree(Location l) {
		int x = l.getX();
		int y = l.getY();
		if(x < 0 || x > board.length) {
			return false;
		}
		if(y < 0 || y > board[x].length) {
			return false;
		}
		
		char c = board[x][y];
		return c == FREE_SPACE || c == GOAL;
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
		char[][] subBoard = new char[width][height];
		for (int row = 0; row < height; row++)
			subBoard[row] = Arrays.copyOfRange(board[y++], x, x + width);
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

	private static GameState fillBoard(char[][] board, List<String> boardStrings) {
		/*
		 * Fill the board using the board strings.
		 */
		Player player = null;
		List<Box> boxes = new ArrayList<Box>();
		for (int row = 0; row < board.length; row++) {
			String rowString = boardStrings.get(row);
			for (int col = 0; col < board[row].length; col++) {
				char square = rowString.charAt(col);
				switch (square) {
				case FREE_SPACE:
				case WALL:
				case GOAL:
					board[row][col] = square;
					break;
				case PLAYER:
				case PLAYER_ON_GOAL:
					player = new Player(new Location(col, row));
					break;
				case BOX:
				case BOX_ON_GOAL:
					Box box = new Box(new Location(col, row));
					boxes.add(box);
					break;
				}
			}
		}
		
		return new GameState(board, player, boxes);
	}
}
