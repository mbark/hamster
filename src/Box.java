
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
}