package fr.theoszanto.sqldatabase.sqlbuilders.dml;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SQLInsertBuilder<T extends SQLInsertBuilder<T>> extends SQLBuilder {
	private @Nullable String table;

	@SuppressWarnings("unchecked")
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T into(@NotNull String table) {
		this.table = table;
		return (T) this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.table == null)
			throw new IllegalStateException("Cannot insert data without table. You must call .into(table) to specify table");

		return "INSERT INTO " + quoteName(this.table);
	}
}
