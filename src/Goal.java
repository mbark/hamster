/**
 * This class represents a goal in a sokoban map.
 */
public class Goal {
	private final Location location;
	
	/**
	 * Create a new {@link Goal} with the given {@link Location}.
	 * 
	 * @param location The location of this Box
	 */
	public Goal(Location location) {
		this.location = location;
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
		if (!(obj instanceof Goal))
			return false;
		Goal b = (Goal) obj;
		return location.equals(b.location);
	}
	
	@Override public String toString() {
		return "Goal: " + location.toString();
	}
}
