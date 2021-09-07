package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class ColumnEntity {
	private final @NotNull TableEntity table;
	private final @NotNull String name;
	private final @NotNull Class<?> type;
	private final @NotNull Field field;

	ColumnEntity(@NotNull TableEntity table, @NotNull String name, @NotNull Class<?> type, @NotNull Field field) {
		this.table = table;
		this.name = name;
		this.type = type;
		this.field = field;
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
