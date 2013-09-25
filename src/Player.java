
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

	@Override public Location getLocation() {
		return location;
	}


	@Override public int hashCode() {
		return location.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Player))
			return false;
		Player p = (Player) obj;
		return location.equals(p.location);
	}
	
	@Override public String toString() {
		return "Player: " + location.toString();
	}
}
	
