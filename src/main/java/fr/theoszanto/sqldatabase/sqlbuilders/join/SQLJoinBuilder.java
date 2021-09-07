package fr.theoszanto.sqldatabase.sqlbuilders.join;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SQLJoinBuilder<T extends SQLJoinBuilder<T>> extends SQLBuilder {
	private final @NotNull String prefix;
	private @Nullable String table;
	private @Nullable String alias;

	protected SQLJoinBuilder(@NotNull String prefix) {
		this.prefix = prefix;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T table(@NotNull String table) {
		return this.table(table, null);
	}

	@SuppressWarnings("unchecked")
	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull T table(@NotNull String table, @Nullable String alias) {
		this.table = table;
		this.alias = alias;
		return (T) this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.table == null)
			throw new IllegalStateException("Cannot join without table. You must call .table(table) to set the table");

		// Join alias
		String alias = this.alias == null ? "" : " AS " + quoteName(this.alias);

		return this.prefix + " JOIN " + quoteName(this.table) + alias;
	}
}
