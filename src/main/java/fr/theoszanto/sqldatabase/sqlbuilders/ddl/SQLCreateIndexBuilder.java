package fr.theoszanto.sqldatabase.sqlbuilders.ddl;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLSortOrder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLConditionBuilder;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SQLCreateIndexBuilder extends SQLBuilder {
	private boolean unique = false;
	private boolean ifNotExists = false;
	private @Nullable String index;
	private @Nullable String on;
	private final @NotNull Map<@NotNull String, @NotNull SQLSortOrder> columns = CollectionsUtils.orderedMap();
	private @Nullable SQLConditionBuilder where;

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder unique() {
		return this.unique(true);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder unique(boolean unique) {
		this.unique = unique;
		return this;
	}

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder ifNotExists() {
		return this.ifNotExists(true);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder ifNotExists(boolean ifNotExists) {
		this.ifNotExists = ifNotExists;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder index(@NotNull String index) {
		this.index = index;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder on(@NotNull String on) {
		this.on = on;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder column(@NotNull String column) {
		return this.column(column, SQLSortOrder.ASC);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder column(@NotNull String column, @NotNull SQLSortOrder order) {
		this.columns.put(column, order);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateIndexBuilder where(@Nullable SQLConditionBuilder where) {
		this.where = where;
		return this;
	}

	@Override
	public @NotNull String build() {
		if (this.index == null)
			throw new IllegalStateException("Cannot create index without name. You must call .index(name) to specify the index name");
		if (this.on == null)
			throw new IllegalStateException("Cannot create index on no table. You must call .on(table) to specify the indexed table");
		if (this.columns.isEmpty())
			throw new IllegalStateException("Cannot create index on no column. You must call .column(name[, order]) at least once to specify the indexed column(s)");

		// Unique index
		String unique = this.unique ? "UNIQUE " : "";

		// Create only if not exists
		String ifNotExists = this.ifNotExists ? "IF NOT EXISTS " : "";

		// Indexed columns
		String columns = CollectionsUtils.join(", ", "(", ")", this.columns, (column, sort) -> quoteName(column) + " " + sort.name());

		// Index condition
		String where = this.where == null ? "" : " WHERE " + this.where;

		return "CREATE " + unique + "INDEX " + ifNotExists + quoteName(this.index) + " ON " + quoteName(this.on) + " " + columns + where;
	}
}
