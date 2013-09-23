
/**
 * An enum representing a basic move in the Sokoban game.
 * 
 * @author Fredrik Bystam
 *
 */
public enum Move {

	UP('U', 0, -1) {
		@Override public Move inverse() {
			return DOWN;
		}
	},
	RIGHT('R', 1, 0) {
		@Override public Move inverse() {
			return LEFT;
		}
	},
	DOWN('D', 0, 1) {
		@Override public Move inverse() {
			return UP;
		}
	},
	LEFT('L', -1, 0) {
		@Override public Move inverse() {
			return RIGHT;
		}
	};
	
	private final char moveChar;
	// public because final, for readability
	public final int dx, dy;
	
	Move (char moveChar, int dx, int dy) {
		this.moveChar = moveChar;
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

	public char toChar () {
		return moveChar;
	}
}
