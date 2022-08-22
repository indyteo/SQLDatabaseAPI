package fr.theoszanto.sqldatabase.sqlbuilders;

import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLAlterTableBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLCreateIndexBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLCreateTableBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLDropIndexBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLDropTableBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLDeleteBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLInsertSelectBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLInsertValuesBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLSelectBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLUpdateBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SQLBuilder {
	public static final char QUOTE_NAME = '`';
	public static final char QUOTE_VAL = '\'';
	public static final char DEFAULT_LIKE_ESCAPE_CHAR = '\\';

	private static final @NotNull String QUOTE_NAME_AS_STRING = "" + QUOTE_NAME;
	private static final @NotNull String DOUBLE_QUOTE_NAME = QUOTE_NAME_AS_STRING + QUOTE_NAME_AS_STRING;
	private static final @NotNull String QUOTE_VAL_AS_STRING = "" + QUOTE_VAL;
	private static final @NotNull String DOUBLE_QUOTE_VAL = QUOTE_VAL_AS_STRING + QUOTE_VAL_AS_STRING;

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
			return QUOTE_NAME + name.replace(QUOTE_NAME_AS_STRING, DOUBLE_QUOTE_NAME) + QUOTE_NAME;
		return quoteName(name.substring(0, dot)) + "." + quoteName(name.substring(dot + 1));
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull String quoteVal(@Nullable Object value) {
		return QUOTE_VAL + String.valueOf(value).replace(QUOTE_VAL_AS_STRING, DOUBLE_QUOTE_VAL) + QUOTE_VAL;
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull String escapeLike(@NotNull String like) {
		return escapeLike(like, DEFAULT_LIKE_ESCAPE_CHAR);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull String escapeLike(@NotNull String like, char escape) {
		String escapeStr = Character.toString(escape);
		return like.replaceAll("[%_" + Pattern.quote(escapeStr) + "]", Matcher.quoteReplacement(escapeStr) + "$0");
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLCreateTableBuilder createTable() {
		return new SQLCreateTableBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLAlterTableBuilder alterTable() {
		return new SQLAlterTableBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLDropTableBuilder dropTable() {
		return new SQLDropTableBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLCreateIndexBuilder createIndex() {
		return new SQLCreateIndexBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLDropIndexBuilder dropIndex() {
		return new SQLDropIndexBuilder();
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
}
