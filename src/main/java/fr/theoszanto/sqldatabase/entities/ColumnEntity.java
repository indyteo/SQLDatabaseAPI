package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.sql.Date;

public class ColumnEntity {
	private final @NotNull TableEntity table;
	private final @NotNull String name;
	private final @NotNull Class<?> type;
	private final @NotNull Field field;
	private final boolean primary;

	ColumnEntity(@NotNull TableEntity table, @NotNull String name, @NotNull Class<?> type, @NotNull Field field, boolean primary) {
		this.table = table;
		this.name = name;
		this.type = type;
		this.field = field;
		this.primary = primary;
	}

	public @NotNull TableEntity getTable() {
		return this.table;
	}

	public @NotNull String getName() {
		return this.name;
	}

	public @NotNull Class<?> getType() {
		return this.type;
	}

	public @NotNull Field getField() {
		return this.field;
	}

	public boolean isPrimary() {
		return this.primary;
	}

	public boolean isForeign() {
		return !this.type.isPrimitive() && this.type != String.class && this.type != Date.class;
	}

	public @NotNull SQLValue asSQLValue() {
		return SQLValue.column(this.table.getName(), this.name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ColumnEntity column = (ColumnEntity) o;

		if (!table.equals(column.table)) return false;
		return name.equals(column.name);
	}

	@Override
	public int hashCode() {
		int result = table.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}
}
