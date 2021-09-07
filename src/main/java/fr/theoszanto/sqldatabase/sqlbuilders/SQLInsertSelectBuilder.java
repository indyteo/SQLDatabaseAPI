package fr.theoszanto.sqldatabase.sqlbuilders;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLInsertSelectBuilder extends SQLInsertBuilder<SQLInsertSelectBuilder> {
	private @Nullable SQLSelectBuilder select;

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLInsertSelectBuilder select(@NotNull SQLSelectBuilder select) {
		this.select = select;
		return this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.select == null)
			throw new IllegalStateException("Cannot insert data without selection source. You must call .select(select) at least once to specify data to insert");

		return super.build() + " " + this.select;
	}
}
