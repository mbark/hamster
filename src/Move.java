
/**
 * An enum representing a basic move in the Sokoban game.
 * 
 * @author Fredrik Bystam
 *
 */
public enum Move {

	UP(0, -1),
	RIGHT(1, 0),
	DOWN(0, 1),
	LEFT(-1, 0);
	
	public final int dx, dy;
	
	Move (int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
}
