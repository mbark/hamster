
/**
 * An interface declaring that an implementation can be affected by a {@link Move}.
 * 
 * @author Fredrik Bystam
 *
 * @param <T> The resulting type of {@link #move(Move)}. Typically the implementing type.
 */
public interface Movable<T> {

	/**
	 * Performs a move upon this {@link Movable} instance. The result
	 * of invoking {@link #move(Move)} is a new instance of type T,
	 * where the move has affected it.
	 * @param move The {@link Move} to perform upon this {@link Movable}
	 * @return A new instance of T altered by the move
	 */
	T move (Move move);
}
