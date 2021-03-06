package fr.theoszanto.sqldatabase.sqlbuilders.dml;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SQLConditionBuilder extends SQLBuilder {
	private final @NotNull StringBuilder condition;

	private SQLConditionBuilder(@NotNull String condition) {
		this.condition = new StringBuilder(condition.length()).append(condition);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	private @NotNull SQLConditionBuilder combinaison(@NotNull String operator, @NotNull SQLConditionBuilder condition) {
		this.condition.insert(0, '(').append(") ").append(operator).append(" (").append(condition).append(')');
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLConditionBuilder and(@NotNull SQLConditionBuilder condition) {
		return this.combinaison("AND", condition);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLConditionBuilder or(@NotNull SQLConditionBuilder condition) {
		return this.combinaison("OR", condition);
	}

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLConditionBuilder not() {
		this.condition.insert(0, "NOT (").append(')');
		return this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		return this.condition.toString();
	}

	@Contract(value = "_, _, _ -> new", pure = true)
	private static @NotNull SQLConditionBuilder comparison(@NotNull SQLValue val1, @NotNull String operator, @NotNull SQLValue val2) {
		return new SQLConditionBuilder(val1 + " " + operator + " " + val2);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder equals(@NotNull SQLValue val1, @NotNull SQLValue val2) {
		return comparison(val1, "=", val2);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder notEquals(@NotNull SQLValue val1, @NotNull SQLValue val2) {
		return comparison(val1, "<>", val2);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder lower(@NotNull SQLValue val1, @NotNull SQLValue val2) {
		return comparison(val1, "<", val2);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder greater(@NotNull SQLValue val1, @NotNull SQLValue val2) {
		return comparison(val1, ">", val2);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder lowerEquals(@NotNull SQLValue val1, @NotNull SQLValue val2) {
		return comparison(val1, "<=", val2);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder greaterEquals(@NotNull SQLValue val1, @NotNull SQLValue val2) {
		return comparison(val1, ">=", val2);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder like(@NotNull SQLValue value, @NotNull String pattern) {
		return like(value, pattern, DEFAULT_LIKE_ESCAPE_CHAR);
	}

	@Contract(value = "_, _, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder like(@NotNull SQLValue value, @NotNull String pattern, char escape) {
		return new SQLConditionBuilder(value + " LIKE " + SQLValue.quoted(pattern) + " ESCAPE " + SQLValue.quoted(escape));
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder inList(@NotNull SQLValue value, @NotNull SQLValue... values) {
		return comparison(value, "IN", SQLValue.list(values));
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder inSelect(@NotNull SQLValue value, @NotNull SQLSelectBuilder select) {
		return comparison(value, "IN", SQLValue.select(select));
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull SQLConditionBuilder exists(@NotNull SQLSelectBuilder select) {
		return new SQLConditionBuilder("EXISTS (" + select + ")");
	}

	@Contract(value = "_, _, _ -> new", pure = true)
	public static @NotNull SQLConditionBuilder between(@NotNull SQLValue value, @NotNull SQLValue min, @NotNull SQLValue max) {
		return new SQLConditionBuilder(value + " BETWEEN " + min + " AND " + max);
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLConditionBuilder alwaysTrue() {
		return new SQLConditionBuilder("TRUE");
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLConditionBuilder alwaysFalse() {
		return new SQLConditionBuilder("FALSE");
	}
}
