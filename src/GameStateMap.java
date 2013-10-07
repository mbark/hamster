import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple Map implementation that does NOT tolerate null insersions.
 */
public class GameStateMap implements Map<GameState, GameState> {
	
	private static final int BUCKET_SIZE = 1024;
	private static final int BUCKET_DEPTH = 32;
	
	private final List<GameStateEntry>[] buckets;
	private int size = 0;

	@SuppressWarnings("unchecked")
	public GameStateMap() {
		buckets = (List<GameStateEntry>[]) new ArrayList[BUCKET_SIZE];
	}

	@Override public int size() {
		return size;
	}

	@Override public boolean isEmpty() {
		return size == 0;
	}

	@Override public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("Method not implemented");
//		for (int i = 0; i < BUCKET_SIZE; i++)
//			if (buckets[i] != null)
//				for (int k = 0; k < buckets[i].size(); k++)
//					if (buckets[i].get(k).getValue().equals(value))
//						return true;
//		return false;
	}

	@Override public GameState get(Object key) {
		int hash = hash(key);
		initBucket(hash);
		List<GameStateEntry> bucket = buckets[hash];
		for (int i = 0; i < bucket.size(); i++) {
			GameStateEntry gse = bucket.get(i);
			if (gse.getKey().equals(key))
				return gse.getValue();
		}
		return null;
	}

	@Override public GameState put(GameState key, GameState value) {
		int hash = hash(key);
		initBucket(hash);
		List<GameStateEntry> bucket = buckets[hash];
		for (int i = 0; i < bucket.size(); i++) {
			GameStateEntry gse = bucket.get(i);
			if (gse.getKey().equals(key))
				return gse.setValue(value);
		}
		bucket.add(new GameStateEntry(key, value));
		return null;
	}

	@Override public GameState remove(Object key) {
		int hash = hash(key);
		initBucket(hash);
		List<GameStateEntry> bucket = buckets[hash];
		for (Iterator<GameStateEntry> it = bucket.iterator(); it.hasNext();) {
			GameStateEntry gse = it.next();
			if (gse.getKey().equals(key)) {
				GameState old = gse.getValue();
				it.remove();
				return old;
			}
		}
		return null;
	}

	@Override public void putAll(Map<? extends GameState, ? extends GameState> m) {
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override public void clear() {
		throw new UnsupportedOperationException("Method not implemented");
	}

	@Override public Set<GameState> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Collection<GameState> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Set<java.util.Map.Entry<GameState, GameState>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private int hash (Object o) {
		int hash = Math.abs(o.hashCode() % buckets.length);
		return hash;
	}
	
	private void initBucket (int bucketIndex) {
		if (buckets[bucketIndex] == null)
			buckets[bucketIndex] = new ArrayList<>(BUCKET_DEPTH);
	}
	
	private static class GameStateEntry implements Map.Entry<GameState, GameState> {
		
		private final GameState key;
		private GameState value;
		
		public GameStateEntry(GameState key, GameState value) {
			this.key = key;
			this.value = value;
		}

		@Override public GameState getKey() {
			return key;
		}

		@Override public GameState getValue() {
			return value;
		}

		@Override public GameState setValue(GameState value) {
			GameState old = this.value;
			this.value = value;
			return old;
		}
	}
}
