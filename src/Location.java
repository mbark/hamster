
public class Location implements Movable<Location> {
	
	private int x;
	private int y;
	
	public Location (int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override public Location move(Move move) {
		return new Location(x + move.dx, y + move.dy);
	}
}
