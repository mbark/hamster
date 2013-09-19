import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


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
 * If the map is of a non-rectangular shape, excessive space will be filled with walls.
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
	
	// very simple test code
	public static void main(String[] args) {
		Scanner s = new Scanner (System.in);
		List<String> boardStrings = new ArrayList<>();
		while (s.hasNextLine ())
			boardStrings.add(s.nextLine());
		GameState l = GameState.createInitialFromInput(boardStrings);
		System.out.println(l);
		s.close();
		System.out.println(l.subGameState(1, 1, 4, 3));
	}
	
	private final char[][] board;
	
	// internal constructor
	GameState (char[][] board) {
		this.board = board;
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
	
	/**
	 * Create a new {@link GameState} using the given input strings.
	 * 
	 * @param boardStrings Strings read as a level map
	 */
	public static GameState createInitialFromInput (List<String> boardStrings)  {
		char[][] board = calculateBoard (boardStrings);
		return new GameState (board);
	}
	
	private static char[][] calculateBoard (List<String> boardStrings) {
		int height = boardStrings.size();
		int width = 0;
		for (String s : boardStrings)
			width = Math.max(width, s.length());
		char[][] board = new char[height][width];
		fillBoard (board, boardStrings);
		return board;
	}
	
	private static void fillBoard (char[][] board, List<String> boardStrings) {
		/*
		 * Fill the board using the board strings.
		 * 
		 * For convenience, free space outside the map will be filled with
		 * WALL-characters to create a complete, rectangular map matrix.
		 */
		for (int row = 0; row < board.length; row++) {
			String rowString = boardStrings.get(row);
			boolean leftOfWall = true; // used to fill outer free space with WALL
			for (int col = 0; col < board[row].length; col++) {
				if (col >= rowString.length()) {
					board[row][col] = WALL;
				} else {
					char square = rowString.charAt(col);
					if (square == WALL)
						leftOfWall = false;
					if (leftOfWall)
						board[row][col] = WALL;
					else
						board[row][col] = square;					
				}
			}
		}
	}
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				sb.append(board[row][col]);
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
