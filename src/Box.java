
/**
 * A class representing a box in the Sokoban game.
 * <p>
 * A box is {@link Movable}.
 * @author Fredrik Bystam
 *
 */
public class Box implements Movable<Box> {
	
	private final Location location;
	
	/**
	 * Create a new {@link Box} with the given {@link Location}.
	 * 
	 * @param location The location of this Box
	 */
	public Box (Location location) {
		this.location = location;
	}

	@Override public Box move(Move move) {
		return new Box(location.move(move));
	}
	
	public Location getLocation() {
		return location;
	}

	@Override public int hashCode() {
		return location.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Box))
			return false;
		Box b = (Box) obj;
		return location.equals(b.location);
	}
}
