import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a state of the game, with the current map and the positions of all the
 * moving objects in it.
 * <p>
 * Has a level with an existing board. A board could look like this:
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
 * The origin point (0,0) is defined in the top left corner of the map (walls included).
 * <p>
 * If the map is of a non-rectangular shape, excessive space will be "free space".
 * 
 * @author Fredrik Bystam
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
	private final Location player = new Location(0, 0);
	private final List<Location> boxes = new ArrayList<>();
	
	// internal constructor
	GameState (char[][] board) {
		this.board = board;
	}
	
	/**
	 * Create a new {@link GameState} using the given input strings.
	 * 
	 * @param boardStrings Strings read as a level map
	 */
	public GameState (List<String> boardStrings)  {
		board = calculateBoard (boardStrings);
	}
	
	/**
	 * Get all the significant {@link GameState} instances that are the result
	 * of one state change in the map.
	 * 
	 * @return {@link List} of {@link GameState} objects that are "one step away"
	 */
	public List<GameState> getNextStates() {
		List<GameState> nextStates = new ArrayList<>();
		// ********** temporary shitty code to calculate next states ***********
		
		
		
		// *********************************************************************
		return nextStates;
	}

	/**
	 * Create a {@link GameState} object as a sub-level of this GameState given a
	 * rectangular shape.
	 * @param x The x-coordinate that will mark the sub-Levels origin
	 * @param y The y-coordinate that will mark the sub-Levels origin
	 * @param width The width of the sub-Level
	 * @param height The height of the sub-Level
	 * @return A {@link GameState} object with a sub-matrix of the original Level
	 */
	public GameState subGameState (int x, int y, int width, int height) {
		char[][] subBoard = new char[width][height];
		for (int row = 0; row < height; row++)
			subBoard[row] = Arrays.copyOfRange(board[y++], x, x + width);
		return new GameState (subBoard);
	}

	
	private char[][] calculateBoard (List<String> boardStrings) {
		int height = boardStrings.size();
		int width = 0;
		for (String s : boardStrings)
			width = Math.max(width, s.length());
		char[][] board = new char[height][width];
		fillBoard (board, boardStrings);
		return board;
	}
	
	private void fillBoard (char[][] board, List<String> boardStrings) {
		/*
		 * Fill the board using the board strings.
		 */
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
					player.setX(col);
					player.setY(row);
					break;
				case BOX:
				case BOX_ON_GOAL:
					boxes.add(new Location(col, row));
					break;
				}
			}
		}
	}	
}
