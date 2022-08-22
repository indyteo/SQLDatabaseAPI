package fr.theoszanto.sqldatabase.sqlbuilders.ddl;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint.SQLConstraintBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLSelectBuilder;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLCreateTableBuilder extends SQLBuilder {
	private boolean temporary = false;
	private boolean ifNotExists = false;
	private @Nullable String table;
	private @Nullable SQLSelectBuilder as;
	private final @NotNull List<@NotNull Column> columns = new ArrayList<>();
	private final @NotNull Set<@NotNull SQLConstraintBuilder<?>> constraints = new HashSet<>();
	private final @NotNull Set<@NotNull Option> options = new HashSet<>();

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder temporary() {
		return this.temporary(true);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder temporary(boolean temporary) {
		this.temporary = temporary;
		return this;
	}

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder ifNotExists() {
		return this.ifNotExists(true);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder ifNotExists(boolean ifNotExists) {
		this.ifNotExists = ifNotExists;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder table(@NotNull String table) {
		this.table = table;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder as(@NotNull SQLSelectBuilder as) {
		this.as = as;
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder column(@NotNull String column, @Nullable SQLConstraintBuilder<?> @NotNull... constraint) {
		return this.column(column, null, constraint);
	}

	@Contract(value = "_, _, _ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder column(@NotNull String column, @Nullable String type, @Nullable SQLConstraintBuilder<?> @NotNull... constraint) {
		this.columns.add(new Column(column, type, constraint));
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder constraint(@NotNull SQLConstraintBuilder<?> constraint) {
		this.constraints.add(constraint);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCreateTableBuilder option(@NotNull Option option) {
		this.options.add(option);
		return this;
	}

	@Override
	public @NotNull String build() {
		if (this.table == null)
			throw new IllegalStateException("Cannot create table without name. You must call .table(name) to specify the table name");
		if (this.as == null && this.columns.isEmpty())
			throw new IllegalStateException("Cannot create table with neither as select nor a set of columns. You must call either .as(select) or .column(name[, type][, constraints...]) to define the table");
		if (this.as != null && !this.columns.isEmpty())
			throw new IllegalStateException("Cannot create table as select and with a set of columns. You must call either .as(select) or .column(name[, type][, constraints...]) but not both");

		// Temporary table
		String temporary = this.temporary ? "TEMPORARY " : "";

		// Create only if not exists
		String ifNotExists = this.ifNotExists ? "IF NOT EXISTS " : "";

		// Table definition
		String definition;
		if (this.as == null) {
			String columns = CollectionsUtils.join(", ", this.columns);
			if (this.constraints.isEmpty())
				definition = "(" + columns + ")";
			else {
				String constraints = CollectionsUtils.join(", ", this.constraints);
				definition = "(" + columns + ", " + constraints + ")";
			}
		} else
			definition = "AS " + this.as.build();

		// Table options
		String options = CollectionsUtils.join(", ", " ", "", this.options);

		return "CREATE " + temporary + "TABLE " + ifNotExists + quoteName(this.table) + " " + definition + options;
	}

	public enum Option {
		WITHOUT_ROWID, STRICT;

		@Override
		public @NotNull String toString() {
			return this.name().replace('_', ' ');
		}
	}

	/* package-private */ static class Column implements Comparable<Column> {
		private final @NotNull String name;
		private final @Nullable String type;
		// TODO Create ColumnConstraint type that differs from table Constraint
		private final @NotNull List<@Nullable SQLConstraintBuilder<?>> constraint;

		public Column(@NotNull String name, @Nullable String type, @Nullable SQLConstraintBuilder<?> @NotNull... constraint) {
			this.name = name;
			this.type = type;
			this.constraint = Arrays.asList(constraint);
		}

		@Override
		public String toString() {
			return SQLBuilder.quoteName(this.name) + (this.type == null ? "" : " " + this.type) + (this.constraint.isEmpty() ? "" : " " + CollectionsUtils.join(" ", this.constraint));
		}

		@Override
		public int compareTo(@NotNull Column o) {
			return this.name.compareTo(o.name);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Column column = (Column) o;
			return this.name.equals(column.name);
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}
	}
}
