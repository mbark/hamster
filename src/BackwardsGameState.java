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
			newBoxes.add(movedBox);
			moves.addLast (boxMove.move);
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
	

	
	/**
	 * Get all the significant {@link BackwardsGameState} instances that are the result
	 * of one state change in the map.
	 * 
	 * @return {@link List} of {@link BackwardsGameState} objects that are
	 *         "one step away"
	 */
	public List<BackwardsGameState> getNextStates() {
		List<BackwardsGameState> nextStates = new ArrayList<>();
		// ********** temporary shitty code to calculate next states ***********
		
		if(player == null) {
			for(Box box : boxes) {
				List<BackwardsGameState> states = getStatesNextToBox(box);
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
	
	private List<BackwardsGameState> getStatesNextToBox(Box box) {
		List<BackwardsGameState> states = new ArrayList<>(4);
		
		addIfFree(states, box, Move.UP);
		addIfFree(states, box, Move.DOWN);
		addIfFree(states, box, Move.LEFT);
		addIfFree(states, box, Move.RIGHT);
		
		return states;
	}
	
	private void addIfFree(List<BackwardsGameState> states, Box box, Move move) {
		Location loc = box.getLocation().move(move);
		if(isFreeForPlayer(loc)) {
			Player player = new Player(loc);
			states.add(new BackwardsGameState(board, player, boxes));
		}
	}

	// the List thing is a fulhack :( but it works :D maybe :/
	private List<BackwardsGameState> move(Move move) {
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
				return Arrays.asList(new BackwardsGameState(board, movedPlayer, movedBoxes, move),
									 new BackwardsGameState(board, movedPlayer, boxes, move));
			}
		}

		// at this point, no boxes were moved
		return Arrays.asList (new BackwardsGameState(board, movedPlayer, boxes, move));
	}
	
	private boolean boxCanBePulled(Box box, Player player, Move move) {
		// if there is a box in the opposite direction of the move, return true
		Location inverseLocation = player.getLocation().move(move.inverse());
		return box.getLocation().equals(inverseLocation);
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
}
