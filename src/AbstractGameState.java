import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;


public abstract class AbstractGameState implements GameState {
	
	// this is practically a singleton. can be ignored in equals/hashCode
	protected final Board board;
	protected final Deque<Move> movesToHere;
	
	protected final Player player;
	protected final Set<Box> boxes;

	AbstractGameState (Board board, Player player, Set<Box> boxes) {
		this (board, player, boxes, new LinkedList<Move>());
	}

	AbstractGameState(Board board, Player player, Set<Box> boxes, Move lastMove) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
		this.movesToHere = new LinkedList<>();
		movesToHere.addFirst(lastMove);
	}

	AbstractGameState(Board board, Player player, Set<Box> boxes, Deque<Move> movesToHere) {
		this.board = board;
		this.player = player;
		this.boxes = boxes;
		this.movesToHere = movesToHere;
	}

	/**
	 * Get the {@link Move} instance that caused this {@link BackwardsGameState}.
	 * 
	 * @return The final {@link Move} before this {@link BackwardsGameState}
	 */
	@Override public Deque<Move> getMovesToHere () {
		return movesToHere;
	}
	
	@Override public Set<Box> getBoxes() {
		return boxes;
	}
	
	/**
	 * Examines whether all of the given {@link Location}'s are free from both
	 * walls and boxes.
	 * @param locations the Locations to examine
	 * @return <code>true</code> if all the locations are free, <code>false</code> if any of them isn't
	 */
	protected boolean isFreeForPlayer (Location... locations) {
		for (Location loc : locations)
			if (!board.isFree(loc) || boxes.contains(new Box(loc)))
				return false;
		return true;
	}
	
	@Override public int hashCode() {
		int hashCode = 0;
		if(player != null) {
			hashCode += player.hashCode();
		}
		return hashCode + 31*boxes.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof AbstractGameState))
			return false;
		AbstractGameState g = (AbstractGameState) obj;
		return player.equals(g.player) &&
				boxes.equals(g.boxes);
	}
}
