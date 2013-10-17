import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
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
			ForwardsGameState state = new ForwardsGameState(board, movedPlayer, newBoxes, moves);
			if (!isDeadlockState(state, movedBox))
				nextStates.add (state);
		}
		return nextStates;
	}
	
	private boolean isDeadlockState (ForwardsGameState state, Box movedBox) {
		Set<Box> visitedBoxes = new HashSet<>();
		return isDeadlockStateRecursive (state, movedBox, visitedBoxes);
	}

	private boolean isDeadlockStateRecursive(ForwardsGameState state, Box box,
			Set<Box> visitedBoxes) {
		visitedBoxes.add(box);
		if (board.isGoal(box.getLocation()))
			return false; // treat goals as non-deadlocks
		if (!state.getPossibleMoves(box).isEmpty())
			return false;
		
		List<Box> adjacentBoxes = getAdjacentBoxes(box, state.getBoxes(), visitedBoxes);
		for (Box adjacentBox : adjacentBoxes)
			if (!isDeadlockStateRecursive(state, adjacentBox, visitedBoxes))
				return false;
		
		return true;
	}
	
	private List<Box> getAdjacentBoxes (Box box, Set<Box> newBoxes, Set<Box> visitedBoxes) {
		List<Box> adjacentBoxes = new ArrayList<>();
		for (Move move : Move.values()) {
			Box adjacent = box.move(move);
			if (newBoxes.contains(adjacent) && !visitedBoxes.contains(adjacent))
				adjacentBoxes.add(adjacent);
		}
		return adjacentBoxes;
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
		
		board = fillUnreachableLocationsWithWalls(board, player);
		
		Board gameBoard = new Board(board, goals, GOAL);
		preprocess(gameBoard);
		return new ForwardsGameState(gameBoard, player, boxes);
	}
	
	private static char[][] fillUnreachableLocationsWithWalls(char[][] board, Player player) {
		Queue<Location> queue = new LinkedList<>();
		queue.add(player.getLocation());
		Set<Location> visited = new HashSet<>();
		visited.add(player.getLocation());
		
		while (!queue.isEmpty()) {
			Location location = queue.poll();
			
			for (Move move : Move.values()) {
				Location newLocation = location.move(move);
				if (visited.contains(newLocation) || getChar(board, newLocation) == WALL)
					continue;
				visited.add(newLocation);
				queue.add(newLocation);
			}
		}
		
		for(int row = 0; row<board.length; row++) {
			for(int col = 0; col<board[row].length; col++) {
				Location l = new Location(col, row);
				if(!visited.contains(l)) {
					board[row][col] = WALL;
				}
			}
		}
		
		return board;
	}
	
	private static Set<Location> preprocess(Board gameBoard) {
		Set<Location> deadlocks = new HashSet<>();
		Set<Location> entrances = new HashSet<>();
		
		char[][] board = gameBoard.getBoardMatrix();
		
		for(int row = 0; row<board.length; row++) {
			for(int col = 0; col<board[row].length; col++) {
				if(board[row][col] == GOAL || board[row][col] == BOX_ON_GOAL) {
					continue;
				}
				
				Location current = new Location(col, row);
				if(isEntrance(board, current)) {
					entrances.add(current);
				}
				if(isDeadlock(board, current)) {
					deadlocks.add(current);
				}
			}
		}
		
		Map<Location, Location> tunnels = findTunnels(entrances);
		
		gameBoard.setDeadlocks(deadlocks);
		gameBoard.setTunnels(tunnels);
		
		return deadlocks;
	}
	
	private static boolean isDeadlock(char[][] board, Location current) {
		if(board[current.getRow()][current.getCol()] == WALL) {
			return false;
		}
		
		boolean isBlockedUp = isBlocked(board, current.move(Move.UP));
		boolean isBlockedDown = isBlocked(board, current.move(Move.DOWN));
		boolean isBlockedLeft = isBlocked(board, current.move(Move.LEFT));
		boolean isBlockedRight = isBlocked(board, current.move(Move.RIGHT));
		
		if(isBlockedUp || isBlockedDown) {
			return isBlockedLeft || isBlockedRight;
		}
		
		return false;
	}
	
	private static boolean isBlocked(char[][] board, Location loc) {
		int row = loc.getRow();
		int col = loc.getCol();
		
		if(row < 0 || row >= board.length) {
			return true;
		}
		if(col < 0 || col >= board[row].length) {
			return true;
		}
		
		return board[row][col] == WALL;
	}
	
	private static boolean isEntrance(char[][] board, Location loc) {
		if(getChar(board, loc) == WALL) {
			return false;
		}
		
		boolean upBlocked = isBlocked(board, loc.move(Move.UP));
		boolean downBlocked = isBlocked(board, loc.move(Move.DOWN));
		boolean leftBlocked = isBlocked(board, loc.move(Move.LEFT));
		boolean rightBlocked = isBlocked(board, loc.move(Move.RIGHT));
		
		boolean rowDirectionEntrace = upBlocked && downBlocked;
		boolean colDirectionEntrace = leftBlocked && rightBlocked;
		
		return rowDirectionEntrace != colDirectionEntrace;
	}
	
	private static Map<Location, Location> findTunnels(Set<Location> entrances) {
		Map<Location, Location> tunnels = new HashMap<>();
		Set<Location> visited = new HashSet<>();
		
		for(Location location : entrances) {
			if(visited.contains(location)) {
				continue;
			}
			
			Location down = location.move(Move.DOWN);
			Location right = location.move(Move.RIGHT);
			
			if(entrances.contains(down)) {
				while(entrances.contains(down.move(Move.DOWN))) {
					visited.add(down);
					down = down.move(Move.DOWN);
				}
				tunnels.put(location, down);
				tunnels.put(down, location);
			} else if(entrances.contains(right)) {
				while(entrances.contains(right.move(Move.RIGHT))) {
					visited.add(down);
					right = right.move(Move.RIGHT);
				}
				tunnels.put(location, right);
				tunnels.put(right, location);
			}
		}
		
		return tunnels;
	}
	
	private static char getChar(char[][] board, Location loc) {
		return board[loc.getRow()][loc.getCol()];
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
