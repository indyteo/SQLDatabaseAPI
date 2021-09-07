package fr.theoszanto.sqldatabase.sqlbuilders;

import fr.theoszanto.sqldatabase.sqlbuilders.join.SQLJoinBuilder;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLSelectBuilder extends SQLWOLOBuilder<SQLSelectBuilder> {
	private boolean distinct = false;
	private final @NotNull Map<@NotNull SQLValue, @Nullable String> columns = new HashMap<>();
	private final @NotNull Map<@NotNull SQLValue, @Nullable String> tables = new HashMap<>();
	private final @NotNull List<@NotNull SQLJoinBuilder<?>> joins = new ArrayList<>();
	private final @NotNull List<@NotNull String> groups = new ArrayList<>();
	private @Nullable SQLConditionBuilder having;

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLSelectBuilder distinct() {
		return this.distinct(true);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder distinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder column(@NotNull String name) {
		return this.column(name, null);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder column(@NotNull String name, @Nullable String alias) {
		return this.value(SQLValue.column(name), alias);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder value(@NotNull SQLValue value) {
		return this.value(value, null);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder value(@NotNull SQLValue value, @Nullable String alias) {
		this.columns.put(value, alias);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder from(@NotNull String table) {
		return this.from(table, null);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder from(@NotNull String table, @Nullable String alias) {
		this.tables.put(SQLValue.column(table), alias);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder join(@NotNull SQLJoinBuilder<?> join) {
		this.joins.add(join);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder group(@NotNull String column) {
		this.groups.add(column);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLSelectBuilder having(@Nullable SQLConditionBuilder having) {
		this.having = having;
		return this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.having != null && this.groups.isEmpty())
			throw new IllegalStateException("Cannot specify HAVING clause without GROUP BY. You must call .group(column) at least once to use it");

		// Distinct select
		String distinct = this.distinct ? "DISTINCT " : "";

		// Columns to select
		String columns = this.columns.isEmpty() ? "*" : joinAlias(this.columns);

		// Table from select
		String tables = joinAlias(this.tables);

		// Select from
		String from = this.tables.isEmpty() ? "" : " FROM " + tables;

		// Join other tables
		String joins = this.joins.isEmpty() ? "" : " " + CollectionsUtils.join(" ", this.joins);

		// Where Order Limit Offset
		String wolo = super.build();

		// Group select
		String groups = this.groups.isEmpty() ? "" : " GROUP BY " + QUOTE_NAME + String.join(QUOTE_NAME + ", " + QUOTE_NAME, this.groups) + QUOTE_NAME;

		// Having group condition
		String having = this.having == null ? "" : " HAVING " + this.having;

		return "SELECT " + distinct + columns + from + joins + wolo + groups + having;
	}

	private static @NotNull String joinAlias(@NotNull Map<@NotNull SQLValue, @Nullable String> values) {
		List<String> valuesWithAlias = new ArrayList<>();
		for (Map.Entry<SQLValue, String> value : values.entrySet()) {
			String name = value.getKey().toString();
			String alias = value.getValue();
			if (alias != null)
				name += " AS " + quoteName(alias);
			valuesWithAlias.add(name);
		}
		return String.join(", ", valuesWithAlias);
	}
}
