package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.Database;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLAlterTableBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLCreateTableBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLDropTableBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint.SQLConstraintBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint.SQLPrimaryKeyConstraintBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLConditionBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLDeleteBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLInsertValuesBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLSelectBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.join.SQLJoinBuilder;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TableEntity implements Iterable<@NotNull ColumnEntity> {
	private final @NotNull String name;
	private final @NotNull Class<?> type;
	private final @NotNull Map<@NotNull String, @NotNull ColumnEntity> columns = CollectionsUtils.orderedMap();
	private @Nullable PrimaryKeyEntity primaryKey;
	private final @NotNull Map<@NotNull ColumnEntity, @NotNull ForeignKeyEntity> foreignKeys = new HashMap<>();
	private final @NotNull Map<@NotNull String, @NotNull ColumnEntity> columnsByFieldName = new HashMap<>();

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

	public int size() {
		return this.columns.size();
	}

	public @NotNull PrimaryKeyEntity getPrimaryKey() {
		if (this.primaryKey == null)
			throw new IllegalStateException("PrimaryKey not yet defined");
		return this.primaryKey;
	}

	/* package-private */ void setPrimaryKey(@NotNull PrimaryKeyEntity primaryKey) {
		if (this.primaryKey != null)
			throw new IllegalStateException("PrimaryKey already defined");
		this.primaryKey = primaryKey;
	}

	/* package-private */ boolean needsPrimaryKey() {
		return this.primaryKey == null;
	}

	public @NotNull Map<@NotNull ColumnEntity, @NotNull ForeignKeyEntity> getForeignKeys() {
		return this.foreignKeys;
	}

	/* package-private */ void addColumn(@NotNull ColumnEntity column) {
		this.columns.put(column.getName(), column);
		this.columnsByFieldName.put(column.getField().getName(), column);
	}

	public @Nullable ColumnEntity getColumnByFieldName(@NotNull String fieldName) {
		return this.columnsByFieldName.get(fieldName);
	}

	public @NotNull SQLCreateTableBuilder create() {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot create model binding table");
		SQLCreateTableBuilder builder = SQLBuilder.createTable().table(this.name).ifNotExists();
		for (ColumnEntity column : this) {
			String columnName = column.getName();
			String type;
			if (column.isForeign()) {
				ForeignKeyEntity foreignKey = this.foreignKeys.get(column);
				String foreignTableName = foreignKey.getTable().getName();
				ColumnEntity foreignColumn = foreignKey.getReference();
				String foreignColumnName = foreignColumn.getName();
				type = getColumnSQLType(foreignColumn.getType());
				builder.constraint(SQLConstraintBuilder.foreignKey()
						.name("fk_" + this.name + "_" + columnName + "_" + foreignTableName + "_" + foreignColumnName)
						.column(columnName)
						.references(foreignTableName)
						.referencedColumn(foreignColumnName));
			} else
				type = getColumnSQLType(column.getType());
			builder.column(columnName, type);
		}
		// TODO Set primary key on single column with AUTO_INCREMENT if necessary
		SQLPrimaryKeyConstraintBuilder primaryKey = SQLConstraintBuilder.primaryKey().name("pk_" + this.name);
		for (ColumnEntity column : this.getPrimaryKey())
			primaryKey.column(column.getName());
		builder.constraint(primaryKey);
		return builder;
	}

	public @NotNull SQLAlterTableBuilder alter() {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot alter model binding table");
		return SQLBuilder.alterTable().table(this.name);
	}

	public @NotNull SQLDropTableBuilder drop() {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot drop model binding table");
		return SQLBuilder.dropTable().table(this.name).ifExists();
	}

	public @NotNull SQLSelectBuilder select() {
		return this.select(false);
	}

	public @NotNull SQLSelectBuilder select(boolean wherePrimaryKey) {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot select from model binding table");
		SQLSelectBuilder builder = SQLBuilder.select().from(this.name);
		this.addColumns(builder, "");
		if (wherePrimaryKey)
			builder.where(this.getPrimaryKey().condition());
		return builder;
	}

	private void addColumns(@NotNull SQLSelectBuilder builder, @NotNull String prefix) {
		for (ColumnEntity column : this) {
			String columnName = column.getName();
			SQLValue columnValue = column.asSQLValue();
			if (column.isForeign()) {
				ForeignKeyEntity foreignKey = this.foreignKeys.get(column);
				if (foreignKey != null) {
					TableEntity foreignTable = foreignKey.getTable();
					builder.join(SQLJoinBuilder.inner()
							.table(foreignTable.getName())
							.on(SQLConditionBuilder.equals(columnValue, foreignKey.getReference().asSQLValue())));
					foreignTable.addColumns(builder, prefix + columnName + Database.BIND_RECURSION_SEPARATOR);
				}
			}
			builder.value(columnValue, prefix + columnName);
		}
	}

	public @NotNull SQLInsertValuesBuilder insert() {
		return this.insert(!this.getPrimaryKey().isAutoIncrement());
	}

	public @NotNull SQLInsertValuesBuilder insert(boolean includePrimary) {
		return this.insert(includePrimary ? InsertMode.INCLUDE_PRIMARY : InsertMode.GENERATE_PRIMARY);
	}

	public @NotNull SQLInsertValuesBuilder upsert() {
		return this.insert(InsertMode.UPDATE_ON_PRIMARY_CONFLICT);
	}

	public @NotNull SQLInsertValuesBuilder insert(@NotNull InsertMode mode) {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot insert into model binding table");
		SQLInsertValuesBuilder builder = SQLBuilder.insertValues().into(this.name);
		for (ColumnEntity column : this)
			if (mode != InsertMode.GENERATE_PRIMARY || !column.isPrimary())
				builder.value(column.getName(), SQLValue.PLACEHOLDER);
		if (mode == InsertMode.UPDATE_ON_PRIMARY_CONFLICT)
			for (ColumnEntity column : this.getPrimaryKey())
				builder.onConflict(column.getName());
		return builder;
	}

	public @NotNull SQLDeleteBuilder delete() {
		if (this.name.isEmpty())
			throw new IllegalStateException("Cannot delete from model binding table");
		return SQLBuilder.delete().from(this.name).where(this.getPrimaryKey().condition());
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
		return this.type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return this.type.hashCode();
	}

	private static @NotNull String getColumnSQLType(@NotNull Class<?> type) {
		if (type == void.class || type == Void.class)
			return "NULL";
		if (type == String.class || type == Timestamp.class
				|| type == Date.class || type == Time.class)
			return "TEXT";
		if (type == boolean.class || type == Boolean.class
				|| type == byte.class || type == Byte.class
				|| type == short.class || type == Short.class
				|| type == int.class || type == Integer.class
				|| type == long.class || type == Long.class)
			return "INTEGER";
		if (type == float.class || type == Float.class
				|| type == double.class || type == Double.class
				|| type == BigDecimal.class)
			return "REAL";
		return "BLOB";
	}

	public enum InsertMode {
		GENERATE_PRIMARY,
		INCLUDE_PRIMARY,
		UPDATE_ON_PRIMARY_CONFLICT
	}
}
