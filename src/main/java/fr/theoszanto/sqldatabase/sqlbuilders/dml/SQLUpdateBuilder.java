package fr.theoszanto.sqldatabase.sqlbuilders.dml;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLUpdateBuilder extends SQLWOLOARCBuilder<SQLUpdateBuilder> {
	private @Nullable String table;
	private final @NotNull Map<@NotNull String, @NotNull SQLValue> columns = CollectionsUtils.orderedMap();

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLUpdateBuilder table(@NotNull String table) {
		this.table = table;
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLUpdateBuilder set(@NotNull String column, @NotNull SQLValue value) {
		this.columns.put(column, value);
		return this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.table == null)
			throw new IllegalStateException("Cannot update data without table. You must call .table(table) to specify table");
		if (this.columns.isEmpty())
			throw new IllegalStateException("Cannot update no data. You must call .set(column, value) at least once to set some data");

		// Set columns values
		List<String> columns = new ArrayList<>();
		for (Map.Entry<String, SQLValue> column : this.columns.entrySet())
			columns.add(quoteName(column.getKey()) + " = " + column.getValue());
		String set = String.join(", ", columns);

		// Where Order Limit Offset
		String wolo = super.build();

		return "UPDATE " + this.table + " " + set + wolo;
	}
}
