
/**
 * An enum representing a basic move in the Sokoban game.
 * 
 * @author Fredrik Bystam
 *
 */
public enum Move {

	UP(0, -1) {
		@Override public Move inverse() {
			return DOWN;
		}
	},
	RIGHT(1, 0) {
		@Override public Move inverse() {
			return LEFT;
		}
	},
	DOWN(0, 1) {
		@Override public Move inverse() {
			return UP;
		}
	},
	LEFT(-1, 0) {
		@Override public Move inverse() {
			return RIGHT;
		}
	};
	
	public final int dx, dy;
	
	Move (int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public abstract Move inverse ();
	
	public boolean isInXDirection() {
		return dx > 0;
	}
	
	public boolean isInYDirection() {
		return dy > 0;
	}
	
}
