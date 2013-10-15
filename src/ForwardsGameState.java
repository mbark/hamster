import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForwardsGameState extends AbstractGameState {
	
	public static final char GOAL = '.';
	public static final char PLAYER_ON_BOX = '*';
	public static final char BOX = '$';
	public static final char BOX_ON_GOAL = '+';

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override public List<GameState> getNextBoxStates() {
		List<GameState> nextStates = new ArrayList<>();

		List<BoxMove> possibleBoxMoves = new ArrayList<>();
		for (Box box : boxes) {
			List<Move> possibleMoves = getPossibleMoves(box);
			for (Move move : possibleMoves)
				possibleBoxMoves.add (new BoxMove(box, move));
		}
		// TODO lol
		return nextStates;
	}

	@Override public int getDistanceToGoal() {
		// TODO Auto-generated method stub
		return 0;
	}

	private List<Move> getPossibleMoves (Movable<?> m) {
		List<Move> possibleMoves = new ArrayList<>();
		Location oneUp = m.getLocation().move(Move.UP);
		Location oneRight = m.getLocation().move(Move.RIGHT);
		Location oneDown = m.getLocation().move(Move.DOWN);
		Location oneLeft = m.getLocation().move(Move.LEFT);
		if (isFreeForPlayer(oneUp, oneDown)) {
			possibleMoves.add(Move.UP);
			possibleMoves.add(Move.DOWN);
		}
		if (isFreeForPlayer(oneRight, oneLeft)) {
			possibleMoves.add(Move.RIGHT);
			possibleMoves.add(Move.LEFT);
		}
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
		//TODO 
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
		return new ForwardsGameState(new Board(board, goals, GOAL), null, boxes);
	}
}
