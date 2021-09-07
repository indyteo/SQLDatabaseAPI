package fr.theoszanto.sqldatabase.sqlbuilders;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLDeleteBuilder extends SQLWOLOARCBuilder<SQLDeleteBuilder> {
	private @Nullable String table;

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLDeleteBuilder from(@NotNull String table) {
		this.table = table;
		return this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.table == null)
			throw new IllegalStateException("Cannot delete data from no table. You must call .from(table) to specify table");

		// Where Order Limit Offset
		String wolo = super.build();

		return "DELETE FROM " + quoteName(this.table) + wolo;
	}
}
