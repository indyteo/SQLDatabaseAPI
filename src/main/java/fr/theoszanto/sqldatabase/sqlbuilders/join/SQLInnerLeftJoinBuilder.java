package fr.theoszanto.sqldatabase.sqlbuilders.join;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLConditionBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SQLInnerLeftJoinBuilder<T extends SQLJoinBuilder<T>> extends SQLJoinBuilder<T> {
	private @Nullable SQLConditionBuilder on;
	private @Nullable String using;

	protected SQLInnerLeftJoinBuilder(@NotNull String prefix) {
		super(prefix);
	}

	@SuppressWarnings("unchecked")
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T on(@NotNull SQLConditionBuilder condition) {
		this.on = condition;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T using(@NotNull String column) {
		this.using = column;
		return (T) this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.on == null && this.using == null)
			throw new IllegalStateException("Cannot join with neither ON nor USING");
		if (this.on != null && this.using != null)
			throw new IllegalStateException("Cannot join with both ON and USING");

		// Join conditional clause
		String condition = this.using == null ? " ON " + this.on : " USING (" + quoteName(this.using) + ")";

		return super.build() + condition;
	}
}
