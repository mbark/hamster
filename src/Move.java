
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
	private final int colDiff;
	private final int rowDiff;
	
	Move (char moveChar, int colDiff, int rowDiff) {
		this.moveChar = moveChar;
		this.colDiff = colDiff;
		this.rowDiff = rowDiff;
	}
	
	public abstract Move inverse ();
	
	public char toChar () {
		return moveChar;
	}

	public int getColDiff() {
		return colDiff;
	}

	public int getRowDiff() {
		return rowDiff;
	}
}
