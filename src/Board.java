import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board {
	private final char[][] board;
	private final Set<Goal> goals;
	private final char goalChar;
	private Set<Location> deadlocks;
	private Map<TunnelStart, Location> tunnels;
	private List<ForwardsGameState.GoalArea> goalAreas;
	
	public Board(char[][] board, Set<Goal> goals, char goalChar) {
		this.board = board;
		this.goals = goals;
		this.goalChar = goalChar;
		deadlocks = new HashSet<>();
		tunnels = new HashMap<>();
		goalAreas = new ArrayList<>();
	}
	
	public Set<Goal> getGoals() {
		return goals;
	}
	
	public void setDeadlocks(Set<Location> deadlocks) {
		this.deadlocks = deadlocks;
	}
	
	public void addTunnel(Location start, Location end, Move direction) {
		TunnelStart tunnelStart = new TunnelStart(start, direction);
		tunnels.put(tunnelStart, end);
	}
	
	public void setGoalAreas(List<ForwardsGameState.GoalArea> goalAreas) {
		this.goalAreas = goalAreas;
	}
	
	public List<ForwardsGameState.GoalArea> getGoalAreas() {
		return goalAreas;
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
	
	public boolean isStartOfTunnel(Location l, Move direction) {
		return tunnels.containsKey(new TunnelStart(l, direction));
	}
	
	public Location getEndOfTunnel(Location l, Move direction) {
		return tunnels.get(new TunnelStart(l, direction));
	}
	
	public char getCharForLocation(Location loc) {
		return board[loc.getRow()][loc.getCol()];
	}
	
	public boolean isGoal (Location l) {
		return getCharForLocation(l) == goalChar;
	}
	
	private final class TunnelStart {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((direction == null) ? 0 : direction.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TunnelStart other = (TunnelStart) obj;
			if (direction != other.direction)
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			return true;
		}

		Location start;
		Move direction;
		
		TunnelStart(Location start, Move direction) {
			this.start = start;
			this.direction = direction;
		}
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
