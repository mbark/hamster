
/**
 * A class representing the player in the Sokoban game.
 * <p>
 * The player is {@link Movable}.
 * @author Fredrik Bystam
 *
 */
public class Player implements Movable<Player> {
	
	private final Location location;
	
	/**
	 * Create a new {@link Player} with the given {@link Location}.
	 * 
	 * @param location The location of the Player
	 */
	public Player (Location location) {
		this.location = location;
	}

	@Override public Player move(Move move) {
		return new Player(location.move(move));
	}

	public Location getLocation() {
		return location;
	}
}
	
