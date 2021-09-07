package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.Database;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLConditionBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLInsertValuesBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLSelectBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TableEntity implements Iterable<@NotNull ColumnEntity> {
	private final @NotNull String name;
	private final @NotNull Class<?> type;
	private final @NotNull Map<@NotNull String, @NotNull ColumnEntity> columns = CollectionsUtils.orderedMap();
	private @Nullable PrimaryKeyEntity primaryKey;
	private final @NotNull Map<@NotNull ColumnEntity, @NotNull ForeignKeyEntity> foreignKeys = new HashMap<>();

	public TableEntity(@NotNull String name, @NotNull Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public @NotNull String getName() {
		return this.name;
	}

	public @NotNull Class<?> getType() {
		return this.type;
	}

	public @NotNull Map<@NotNull String, @NotNull ColumnEntity> getColumns() {
		return this.columns;
	}

	public @NotNull PrimaryKeyEntity getPrimaryKey() {
		if (this.primaryKey == null)
			throw new IllegalStateException("PrimaryKey not yet defined");
		return this.primaryKey;
	}

	void setPrimaryKey(@NotNull PrimaryKeyEntity primaryKey) {
		if (this.primaryKey != null)
			throw new IllegalStateException("PrimaryKey already defined");
		this.primaryKey = primaryKey;
	}

	boolean needsPrimaryKey() {
		return this.primaryKey == null;
	}

	public @NotNull Map<@NotNull ColumnEntity, @NotNull ForeignKeyEntity> getForeignKeys() {
		return this.foreignKeys;
	}

	public @NotNull SQLSelectBuilder select() {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot select from model binding table");
		SQLSelectBuilder builder = SQLBuilder.select().from(this.name);
		this.addColumns(builder, "");
		return builder.where(this.getPrimaryKey().condition());
	}

	private void addColumns(@NotNull SQLSelectBuilder builder, @NotNull String prefix) {
		for (ColumnEntity column : this) {
			String columnName = column.getName();
			Class<?> columnType = column.getType();
			SQLValue columnValue = column.asSQLValue();
			if (Database.shouldTreatDirectly(columnType))
				builder.value(columnValue, prefix + columnName);
			else {
				ForeignKeyEntity foreignKey = this.foreignKeys.get(column);
				TableEntity foreignTable = foreignKey.getTable();
				builder.join(SQLBuilder.innerJoin()
						.table(foreignTable.getName())
						.on(SQLConditionBuilder.equals(columnValue, foreignKey.getReference().asSQLValue())));
				foreignTable.addColumns(builder, columnName + Database.BIND_RECURSION_SEPARATOR);
			}
		}
	}

	public @NotNull SQLInsertValuesBuilder upsert() {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot insert into model binding table");
		SQLInsertValuesBuilder builder = SQLBuilder.insertValues().into(this.name);
		for (ColumnEntity column : this)
			builder.value(column.getName(), SQLValue.PLACEHOLDER);
		for (ColumnEntity column : this.getPrimaryKey())
			builder.onConflict(column.getName());
		return builder;
	}

	@Override
	public @NotNull Iterator<@NotNull ColumnEntity> iterator() {
		return this.columns.values().iterator();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TableEntity that = (TableEntity) o;

		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}
}
