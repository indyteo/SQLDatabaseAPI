package fr.theoszanto.sqldatabase.sqlbuilders;

import fr.theoszanto.sqldatabase.sqlbuilders.join.SQLCrossJoinBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.join.SQLInnerJoinBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.join.SQLLeftJoinBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SQLBuilder {
	public static final char QUOTE_NAME = '`';
	public static final char QUOTE_VAL = '\'';

	protected SQLBuilder() {}

	@Contract(value = " -> new", pure = true)
	public abstract @NotNull String build();

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return this.build();
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull String quoteName(@NotNull String name) {
		int dot = name.indexOf('.');
		if (dot == -1)
			return QUOTE_NAME + name.replace("`", "``") + QUOTE_NAME;
		return quoteName(name.substring(0, dot)) + "." + quoteName(name.substring(dot + 1));
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull String quoteVal(@Nullable Object value) {
		return QUOTE_VAL + String.valueOf(value).replace("'", "''") + QUOTE_VAL;
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLSelectBuilder select() {
		return new SQLSelectBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLInsertValuesBuilder insertValues() {
		return new SQLInsertValuesBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLInsertSelectBuilder insertSelect() {
		return new SQLInsertSelectBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLUpdateBuilder update() {
		return new SQLUpdateBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLDeleteBuilder delete() {
		return new SQLDeleteBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLInnerJoinBuilder innerJoin() {
		return new SQLInnerJoinBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLLeftJoinBuilder leftJoin() {
		return new SQLLeftJoinBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLCrossJoinBuilder crossJoin() {
		return new SQLCrossJoinBuilder();
	}
}
