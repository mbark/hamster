import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


/**
 * Represents a level with an existing board. A board could look like this:
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
public class Level {
	
	public static final char FREE_SPACE = ' ';
	public static final char WALL = '#';
	public static final char GOAL = '.';
	public static final char PLAYER = '@';
	public static final char PLAYER_ON_GOAL = '+';
	public static final char BOX = '$';
	public static final char BOX_ON_GOAL = '*';
	
	public static void main(String[] args) {
		Scanner s = new Scanner (System.in);
		List<String> boardStrings = new ArrayList<>();
		while (s.hasNextLine ())
			boardStrings.add(s.nextLine());
		Level l = new Level(boardStrings);
		System.out.println(l);
		s.close();
		System.out.println(l.subLevel(1, 1, 4, 3));
	}
	
	private final char[][] board;
	
	/**
	 * Create a new Level using the given input strings.
	 * 
	 * @param boardStrings Strings read as a level map
	 */
	public Level (List<String> boardStrings) {
		board = calculateBoard (boardStrings);
	}
	
	// internal constructor
	Level (char[][] board) {
		this.board = board;
	}

	public Level subLevel (int x, int y, int width, int height) {
		char[][] subBoard = new char[width][height];
		for (int row = 0; row < height; row++)
			subBoard[row] = Arrays.copyOfRange(board[y++], x, x + width);
		return new Level (subBoard);
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
	
	private static void fillBoard(char[][] board, List<String> boardStrings) {
		for (int row = 0; row < board.length; row++) {
			String rowString = boardStrings.get(row);
			boolean leftOfWall = true; // used to fill outer free space will WALL
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
