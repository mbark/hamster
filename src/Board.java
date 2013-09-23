import java.util.Arrays;
import java.util.Set;


public class Board {
	private final char[][] board;
	private final Set<Goal> goals;
	
	public Board(char[][] board, Set<Goal> goals) {
		this.board = board;
		this.goals = goals;
	}
	
	public boolean isFree(Location l) {
		int x = l.getX();
		int y = l.getY();
		if(x < 0 || x > board.length) {
			return false;
		}
		if(y < 0 || y > board[x].length) {
			return false;
		}
		
		char c = board[x][y];
		return c == GameState.FREE_SPACE || c == GameState.GOAL;
	}
	
	public char getCharForLocation(Location loc) {
		return board[loc.getX()][loc.getY()];
	}
	
	public Board subBoard(int x, int y, int width, int height) {
		char[][] subBoard = new char[width][height];
		for (int row = 0; row < height; row++)
			subBoard[row] = Arrays.copyOfRange(board[y++], x, x + width);
		return new Board(subBoard, goals);
	}
}
