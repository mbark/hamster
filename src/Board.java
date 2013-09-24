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
		if(y < 0 || y > board.length) {
			return false;
		}
		if(x < 0 || x > board[y].length) {
			return false;
		}
		
		char c = board[y][x];
		return c == GameState.FREE_SPACE || c == GameState.GOAL;
	}
	
	public char getCharForLocation(Location loc) {
		return board[loc.getY()][loc.getX()];
	}
	
	public Board subBoard(int x, int y, int width, int height) {
		char[][] subBoard = new char[height][width];
		for (int row = 0; row < height; row++)
			subBoard[row] = Arrays.copyOfRange(board[y++], x, x + width);
		//TODO modify goals so that it only contains the goals in the subboard
		return new Board(subBoard, goals);
	}
}
