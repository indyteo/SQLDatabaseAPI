package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class ColumnEntity {
	private final @NotNull TableEntity table;
	private final @NotNull String name;
	private final @NotNull Class<?> type;
	private final @NotNull Field field;
	private final boolean primary;
	private final boolean foreign;

	/* package-private */ ColumnEntity(@NotNull TableEntity table, @NotNull String name, @NotNull Class<?> type, @NotNull Field field, boolean primary, boolean foreign) {
		this.table = table;
		this.name = name;
		this.type = type;
		this.field = field;
		this.primary = primary;
		this.foreign = foreign;
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
		return this.foreign;
	}

	public @NotNull SQLValue asSQLValue() {
		return SQLValue.column(this.table.getName(), this.name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ColumnEntity column = (ColumnEntity) o;
		if (!this.table.equals(column.table)) return false;
		return this.name.equals(column.name);
	}

	@Override
	public int hashCode() {
		int result = this.table.hashCode();
		result = 31 * result + this.name.hashCode();
		return result;
	}
}
