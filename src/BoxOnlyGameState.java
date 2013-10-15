
public class BoxOnlyGameState {

	private final GameState state;

	public BoxOnlyGameState (GameState state) {
		this.state = state;
	}

	@Override public int hashCode() {
		return state.getBoxes().hashCode();
	}

	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof BoxOnlyGameState))
			return false;
		BoxOnlyGameState b = (BoxOnlyGameState) obj;
		return state.getBoxes().equals(b.state.getBoxes());
	}
}
