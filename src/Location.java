
public class Location implements Movable<Location> {
	
	private final int x;
	private final int y;
	
	public Location (int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	@Override public Location move(Move move) {
		return new Location(x + move.dx, y + move.dy);
	}
	
	@Override public int hashCode() {
		return 17 + x + 31*y;
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Location))
			return false;
		Location l = (Location) obj;
		return x == l.x && y == l.y;
	}
}
