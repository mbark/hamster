import java.util.ArrayList;
import java.util.List;


public class Location implements Movable<Location> {
	
	private final int col;
	private final int row;
	
	public Location (int col, int row) {
		this.col = col;
		this.row = row;
	}
	
	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}
	
	@Override public Location move(Move move) {
		return new Location(col + move.getColDiff(), row + move.getRowDiff());
	}

	/**
	 * Given this and another Location that can be found on the same vertical
	 * or horizontal line, this returns the list of moves to go from this to
	 * end.
	 * <p>
	 * If this Location and end are NOT on a straight line, this will throw
	 * an {@link IllegalArgumentException}.
	 * @param end The end location to move to
	 * @return A {@link List} of {@link Move}'s to take you from this to end
	 */
	public List<Move> getLinearPathTo (Location end) {
		if (col - end.col != 0 && row - end.row != 0)
			throw new IllegalArgumentException("Non-linear Locations " + this + " and " +end);
		
		List<Move> linearPath = new ArrayList<>();
		Move move = null;
		if (col - end.col != 0) // horizontal line
			move = col - end.col < 0 ? Move.RIGHT : Move.LEFT;
		else // vertical line
			move = row - end.row < 0 ? Move.DOWN : Move.UP;
		int length = Math.max(Math.abs (col - end.col), Math.abs(row - end.row));
		for (int i = 0; i < length; i++)
			linearPath.add(move);
		return linearPath;
	}
	
	public static int distance(Location from, Location to) {
		return Math.abs(from.col - to.col) + Math.abs(from.row - to.row);
	}
	
	@Override public int hashCode() {
		return 17 + col + 31*row;
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Location))
			return false;
		Location l = (Location) obj;
		return col == l.col && row == l.row;
	}
	
	@Override public String toString() {
		return String.format("(row: %d, col: %d)", row, col);
	}
	
	@Override public Location getLocation() {
		return this;
	}
}
