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
	private final Location player;
	private final List<Location> boxes;

	// internal constructor
	GameState(char[][] board, Location player, List<Location> boxes) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
	}

	/**
	 * Create a new {@link GameState} using the given input strings.
	 * 
	 * @param boardStrings
	 *            Strings read as a level map
	 */
	public GameState(List<String> boardStrings) {
		board = calculateBoard(boardStrings);
		player = new Location(0, 0);
		boxes = new ArrayList<>();
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

		int x = player.getX();
		int y = player.getY();
		GameState up = goUp();
		if(up != null) {
			nextStates.add(up);
		}
		GameState down = goDown();
		if(down != null) {
			nextStates.add(down);
		}
		GameState left = goLeft();
		if(left != null) {
			nextStates.add(left);
		}
		GameState right = goRight();
		if(right != null) {
			nextStates.add(right);
		}
		

		// *********************************************************************
		return nextStates;
	}

	private GameState goUp() {
		return move(false, true);
	}

	private GameState goDown() {
		return move(false, false);
	}

	private GameState goLeft() {
		return move(true, true);
	}

	private GameState goRight() {
		return move(true, false);
	}
	
	private GameState move(boolean moveX, boolean moveUpOrLeft) {
		int x = player.getX();
		int y = player.getY();

		if (moveX) {
			if(moveUpOrLeft) {
				if(x < 0) {
					return null;
				}
				x--;
			} else {
				if(x >= board.length) {
					return null;
				}
				x++;
			}
		} else {
			if(moveUpOrLeft) {
				if(y < 0) {
					return null;
				}
				y--;
			} else {
				if(y >= board[x].length) {
					return null;
				}
				y++;
			}
		}

		char c = board[x][y];
		if(!isFree(c)) {
			return null;
		}

		Location newLocation = new Location(x, y);
		List<Location> newBoxes = new ArrayList<>();
		for (int i = 0; i < boxes.size(); i++) {
			Location box = new Location(boxes.get(i).getX(), boxes.get(i).getY());

			if (newLocation.getY() == box.getY()) {
				if (newLocation.getY() - 1 > 0) {
					if (isFree(board[x][newLocation.getY() - 1])) {
						box.setY(newLocation.getY() - 1);
					}
				}
			}

			newBoxes.add(box);
		}

		return new GameState(board, newLocation, newBoxes);
	}

	private boolean isFree(char c) {
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

	private char[][] calculateBoard(List<String> boardStrings) {
		int height = boardStrings.size();
		int width = 0;
		for (String s : boardStrings)
			width = Math.max(width, s.length());
		char[][] board = new char[height][width];
		fillBoard(board, boardStrings);
		return board;
	}

	private void fillBoard(char[][] board, List<String> boardStrings) {
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