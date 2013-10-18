import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
public class BackwardsGameState extends AbstractGameState {

	public static final char GOAL = '$';
	public static final char PLAYER_ON_BOX = '+';
	public static final char BOX = '.';
	public static final char BOX_ON_GOAL = '*';

	BackwardsGameState (Board board, Player player, Set<Box> boxes) {
		this (board, player, boxes, new LinkedList<Move>());
	}

	BackwardsGameState(Board board, Player player, Set<Box> boxes, Move lastMove) {
		super(board, player, boxes, lastMove);
	}

	BackwardsGameState(Board board, Player player, Set<Box> boxes, Deque<Move> movesToHere) {
		super(board, player, boxes, movesToHere);
	}

	@Override public List<GameState> getNextBoxStates () {
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
		
		Map<BoxMove, Deque<Move>> movePaths = findBackwardsMovePathsBFS (possibleBoxMoves);
		for (Entry<BoxMove, Deque<Move>> pathEntry : movePaths.entrySet()) {
			BoxMove boxMove = pathEntry.getKey();
			Deque<Move> moves = pathEntry.getValue();
			
			Player playerBeforeBoxMove = new Player(boxMove.box.getLocation().move(boxMove.move));
			Player movedPlayer = playerBeforeBoxMove.move(boxMove.move);
			Box movedBox = boxMove.box.move(boxMove.move);
			
			Set<Box> newBoxes = new HashSet<>(boxes);
			newBoxes.remove(boxMove.box);
			moves.addLast (boxMove.move);
			if (board.isStartOfTunnel(movedBox.getLocation(), boxMove.move)) {
				Location start = movedBox.getLocation();
				List<Move> tunnelPath = start.getLinearPathTo(board.getEndOfTunnel(start, boxMove.move));
				for (Move tunnelMove : tunnelPath) {
					if (boxes.contains(new Box(movedPlayer.move(tunnelMove).getLocation())))
						break;
					movedPlayer = movedPlayer.move(tunnelMove);
					movedBox = movedBox.move(tunnelMove);
					moves.addLast (tunnelMove);
				}
			}
			newBoxes.add(movedBox);
			BackwardsGameState state = new BackwardsGameState(board, movedPlayer, newBoxes, moves);
			nextStates.add (state);
		}
		return nextStates;
	}

	private List<GameState> createInitialStates(List<BoxMove> possibleBoxMoves) {
		List<GameState> initialStates = new ArrayList<>();
		for (BoxMove boxMove : possibleBoxMoves) {
			Player initialPlayer =
					new Player(boxMove.box.getLocation().move(boxMove.move));
			BackwardsGameState initialState = new BackwardsGameState(board, initialPlayer, boxes);
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
		GameState endState = getPlayerMoveGameState(playerEndLocation);
		if(endState == null) // can't find path to "start" from here
			return Collections.emptyList();
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
	
	@Override public boolean isDone() {
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
	
	/**
	 * Create a {@link BackwardsGameState} object as a sub-level of this GameState given
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
	 * @return A {@link BackwardsGameState} object with a sub-matrix of the original
	 *         Level
	 */
	public BackwardsGameState subGameState(int x, int y, int width, int height) {
		Board subBoard = board.subBoard(x, y, width, height);
		return new BackwardsGameState(subBoard, player, boxes);
	}

	public static BackwardsGameState calculateBoard(List<String> boardStrings) {
		int height = boardStrings.size();
		int width = 0;
		for (String s : boardStrings)
			width = Math.max(width, s.length());
		char[][] board = new char[height][width];
		return fillBoard(board, boardStrings);
	}

	private static BackwardsGameState fillBoard(char[][] board, List<String> boardStrings) {
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
		return new BackwardsGameState(new Board(board, goals, GOAL), null, boxes);
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

	@Override public List<GameState> tryGoalMacro() {
		return null;//No goal areas in backwards
	}
	
	
}
