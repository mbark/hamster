
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
