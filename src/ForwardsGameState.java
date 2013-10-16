import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ForwardsGameState extends AbstractGameState {
	
	public static final char GOAL = '.';
	public static final char PLAYER_ON_GOAL = '+';
	public static final char BOX = '$';
	public static final char BOX_ON_GOAL = '*';
	
	ForwardsGameState (Board board, Player player, Set<Box> boxes) {
		super(board, player, boxes);
	}
	
	ForwardsGameState (Board board, Player player, Set<Box> boxes, Move lastMove) {
		super(board, player, boxes, lastMove);
	}

	ForwardsGameState (Board board, Player player, Set<Box> boxes, Deque<Move> movesToHere) {
		super(board, player, boxes, movesToHere);
	}

	@Override public boolean isDone() {
		for(Box box : boxes) {
			Location loc = box.getLocation();
			char c = board.getCharForLocation(loc);
			
			if(c != GOAL) {
				return false;
			}
		}
		return true;
	}

	@Override public List<GameState> getNextBoxStates() {
		List<GameState> nextStates = new ArrayList<>();

		List<BoxMove> possibleBoxMoves = new ArrayList<>();
		for (Box box : boxes) {
			List<Move> possibleMoves = getPossibleMoves(box);
			for (Move move : possibleMoves)
				possibleBoxMoves.add (new BoxMove(box, move));
		}
		
		// this generates BoxMoves mapped to the paths they would require
		// if you were to PULL them. we therefore need to treat them
		// "backwards" to obtain forwards GameStates
		Map<BoxMove, Deque<Move>> movePaths = findBackwardsMovePathsBFS(possibleBoxMoves);
		for (Entry<BoxMove, Deque<Move>> pathEntry : movePaths.entrySet()) {
			BoxMove boxMove = pathEntry.getKey();
			Deque<Move> moves = pathEntry.getValue();
			Box box = boxMove.box;
			Move realMove = boxMove.move.inverse(); // here is the double backwards part
			
			Player playerBeforeBoxMove = new Player(box.getLocation().move(boxMove.move));
			Player movedPlayer = playerBeforeBoxMove.move(realMove);
			Box movedBox = box.move(realMove);
			
			Set<Box> newBoxes = new HashSet<>(boxes);
			newBoxes.remove(box);
			newBoxes.add(movedBox);
			moves.addLast (realMove);
			GameState state = new ForwardsGameState(board, movedPlayer, newBoxes, moves);
			nextStates.add (state);
		}
		return nextStates;
	}

	private List<Move> getPossibleMoves (Movable<?> m) {
		List<Move> possibleMoves = new ArrayList<>();
		Location oneUp = m.getLocation().move(Move.UP);
		Location oneRight = m.getLocation().move(Move.RIGHT);
		Location oneDown = m.getLocation().move(Move.DOWN);
		Location oneLeft = m.getLocation().move(Move.LEFT);
		if (isFreeForPlayer(oneUp, oneDown)) {
			if(!board.isDeadlockLocation(oneDown)) {
				possibleMoves.add(Move.UP);
			}
			if(!board.isDeadlockLocation(oneUp)) {
				possibleMoves.add(Move.DOWN);
			}
		}
		if (isFreeForPlayer(oneRight, oneLeft)) {
			if(!board.isDeadlockLocation(oneLeft)) {
				possibleMoves.add(Move.RIGHT);
			}
			if(!board.isDeadlockLocation(oneRight)) {
				possibleMoves.add(Move.LEFT);
			}
		}
//		System.out.println(m + " " + possibleMoves);
//		System.out.println(toString());
		return possibleMoves;
	}
	
	public static ForwardsGameState calculateBoard(List<String> boardStrings) {
		int height = boardStrings.size();
		int width = 0;
		for (String s : boardStrings)
			width = Math.max(width, s.length());
		char[][] board = new char[height][width];
		return fillBoard(board, boardStrings);
	}

	private static ForwardsGameState fillBoard(char[][] board, List<String> boardStrings) {
		/*
		 * Fill the board using the board strings.
		 */
		Player player = new Player(new Location(-1, -1));
		Set<Box> boxes = new HashSet<Box>();
		Set<Goal> goals = new HashSet<Goal>();
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
				case PLAYER_ON_GOAL:
					board[row][col] = GOAL;
					goals.add(new Goal(new Location(col, row)));
					player = new Player(new Location(col, row));
					break;
				case PLAYER:
					board[row][col] = FREE_SPACE;
					player = new Player(new Location(col, row));
					break;
				case BOX_ON_GOAL:
					boxes.add(new Box(new Location(col, row)));
				case GOAL:
					board[row][col] = GOAL;
					goals.add(new Goal(new Location(col, row)));
					break;
				case BOX:
					board[row][col] = FREE_SPACE;
					boxes.add(new Box(new Location(col, row)));
					break;
				}
			}
		}
		Set<Location> deadlocks = findDeadlocks(board);
		Board gameBoard = new Board(board, goals, GOAL);
		gameBoard.setDeadlocks(deadlocks);
		return new ForwardsGameState(gameBoard, player, boxes);
	}
	
	private static Set<Location> findDeadlocks(char[][] board) {
		Set<Location> deadlocks = new HashSet<>();
		
		for(int row = 0; row<board.length; row++) {
			for(int col = 0; col<board[row].length; col++) {
				if(board[row][col] == GOAL || board[row][col] == BOX_ON_GOAL) {
					continue;
				}
				if(isDeadlock(board, row, col)) {
					deadlocks.add(new Location(col, row));
				}
			}
		}
		
		return deadlocks;
	}
	
	private static boolean isDeadlock(char[][] board, int row, int col) {
		if(board[row][col] == WALL) {
			return false;
		}
		
		boolean isBlockedUp = isBlocked(board, row-1, col);
		boolean isBlockedDown = isBlocked(board, row+1, col);
		boolean isBlockedLeft = isBlocked(board, row, col-1);
		boolean isBlockedRight = isBlocked(board, row, col+1);
		
		if(isBlockedUp || isBlockedDown) {
			return isBlockedLeft || isBlockedRight;
		}
		
		return false;
	}
	
	private static boolean isBlocked(char[][] board, int row, int col) {
		if(row < 0 || row >= board.length) {
			return false;
		}
		if(col < 0 || col >= board[row].length) {
			return false;
		}
		
		return board[row][col] == WALL;
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
			char c = board.getCharForLocation(pl);
			if (c == GOAL)
				matrix[pl.getRow()][pl.getCol()] = PLAYER_ON_GOAL;
			else
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
