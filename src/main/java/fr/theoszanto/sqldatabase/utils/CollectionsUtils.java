package fr.theoszanto.sqldatabase.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class CollectionsUtils {
	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull String join(@NotNull String delimiter, @Nullable Object @NotNull[] values) {
		return join(delimiter, "", "", values);
	}

	@Contract(value = "_, _, _, _ -> new", pure = true)
	public static @NotNull String join(@NotNull String delimiter, @NotNull String prefix, @NotNull String suffix, @Nullable Object @NotNull[] values) {
		StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
		for (Object value : values)
			joiner.add(String.valueOf(value));
		return joiner.toString();
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull String join(@NotNull String delimiter, @NotNull List<?> values) {
		return join(delimiter, "", "", values);
	}

	@Contract(value = "_, _, _, _ -> new", pure = true)
	public static @NotNull String join(@NotNull String delimiter, @NotNull String prefix, @NotNull String suffix, @NotNull List<?> values) {
		StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
		for (Object value : values)
			joiner.add(String.valueOf(value));
		return joiner.toString();
	}

	public static <K, V> @NotNull Map<K, V> orderedMap() {
		return new OrderedMap<>();
	}

	private static class OrderedMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
		private final @NotNull EntrySet entrySet = new EntrySet();

		@Override
		public @Nullable V put(K key, V value) {
			Iterator<Map.Entry<K, V>> i = entrySet.iterator();
			if (key == null) {
				while (i.hasNext()) {
					Map.Entry<K, V> e = i.next();
					if (e.getKey() == null)
						return e.setValue(value);
				}
			} else {
				while (i.hasNext()) {
					Map.Entry<K, V> e = i.next();
					if (key.equals(e.getKey()))
						return e.setValue(value);
				}
			}
			entrySet.add(key, value);
			return null;
		}

		@Override
		public @NotNull Set<Map.Entry<K, V>> entrySet() {
			return entrySet;
		}

		private static class Entry<K, V> implements Map.Entry<K, V> {
			private final K key;
			private V value;

			private Entry(K key, V value) {
				this.key = key;
				this.value = value;
			}

			@Override
			public K getKey() {
				return key;
			}

			@Override
			public V getValue() {
				return value;
			}

			@Override
			public V setValue(V value) {
				V old = this.value;
				this.value = value;
				return old;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;

				Entry<?, ?> entry = (Entry<?, ?>) o;

				return Objects.equals(key, entry.key);
			}

			@Override
			public int hashCode() {
				return key != null ? key.hashCode() : 0;
			}
		}

		private class EntrySet extends AbstractCollection<Map.Entry<K, V>> implements Set<Map.Entry<K, V>> {
			private final @NotNull List<@NotNull Entry<K, V>> content = new ArrayList<>();

			private void add(K key, V value) {
				add(new Entry<>(key, value));
			}

			@Override
			public boolean add(Map.Entry<K, V> e) {
				return content.add((Entry<K, V>) e);
			}

			@Override
			public int size() {
				return content.size();
			}

			@Override
			public @NotNull Iterator<Map.Entry<K, V>> iterator() {
				return new EntryIterator();
			}

			@Override
			public void clear() {
				content.clear();
			}

			private class EntryIterator implements Iterator<Map.Entry<K, V>> {
				private int i = -1;

				@Override
				public boolean hasNext() {
					return i + 1 < content.size();
				}

				@Override
				public Map.Entry<K, V> next() {
					if (this.hasNext())
						return content.get(++i);
					throw new NoSuchElementException();
				}

				@Override
				public void remove() {
					content.remove(i);
				}
			}
		}
	}
}
