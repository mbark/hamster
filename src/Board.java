import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Board {
	private final char[][] board;
	private final Set<Goal> goals;
	private final char goalChar;
	private Set<Location> deadlocks;
	
	public Board(char[][] board, Set<Goal> goals, char goalChar) {
		this.board = board;
		this.goals = goals;
		this.goalChar = goalChar;
		deadlocks = new HashSet<>();
	}
	
	public Set<Goal> getGoals() {
		return goals;
	}
	
	public void setDeadlocks(Set<Location> deadlocks) {
		this.deadlocks = deadlocks;
	}
	
	public boolean isFree(Location l) {
		int col = l.getCol();
		int row = l.getRow();
		if(row < 0 || row >= board.length) {
			return false;
		}
		if(col < 0 || col >= board[row].length) {
			return false;
		}
		
		char c = board[row][col];
		return c == GameState.FREE_SPACE || c == goalChar || c == GameState.PLAYER;
	}
	
	public boolean isDeadlockLocation(Location l) {
		return deadlocks.contains(l);
	}
	
	public char getCharForLocation(Location loc) {
		return board[loc.getRow()][loc.getCol()];
	}
	
	public boolean isGoal (Location l) {
		return getCharForLocation(l) == goalChar;
	}
	
	
	public Board subBoard(int col, int row, int width, int height) {
		char[][] subBoard = new char[height][width];
		for (int i = 0; i < height; i++)
			subBoard[i] = Arrays.copyOfRange(board[row++], col, col + width);
		//TODO modify goals so that it only contains the goals in the subboard
		return new Board(subBoard, goals, goalChar);
	}
	
	public Location getPlayerEndLocation () {
		for (int row = 0; row < board.length; row++)
			for (int col = 0; col < board[row].length; col++)
				if (board[row][col] == GameState.PLAYER)
					return new Location(col, row);
		return null;
	}
	
	char[][] getBoardMatrix () {
		return matrixCopy (board);
	}
	
	static char[][] matrixCopy(char[][] matrix) {
		char[][] copy = new char[matrix.length][];
		for (int row = 0; row < copy.length; row++)
			copy[row] = matrix[row].clone();
		return copy;
	}
}
