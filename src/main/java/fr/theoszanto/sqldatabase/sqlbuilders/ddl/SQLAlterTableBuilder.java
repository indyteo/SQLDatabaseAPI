package fr.theoszanto.sqldatabase.sqlbuilders.ddl;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint.SQLConstraintBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLAlterTableBuilder extends SQLBuilder {
	private @Nullable String table;
	private @Nullable String renameTable;
	private @Nullable String renameColumn, newColumnName;
	private @Nullable SQLCreateTableBuilder.Column addColumn;
	private @Nullable String dropColumn;

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLAlterTableBuilder table(@NotNull String table) {
		this.table = table;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLAlterTableBuilder renameTable(@NotNull String renameTo) {
		this.renameTable = renameTo;
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLAlterTableBuilder renameColumn(@NotNull String column, @NotNull String newName) {
		this.renameColumn = column;
		this.newColumnName = newName;
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLAlterTableBuilder addColumn(@NotNull String column, @Nullable SQLConstraintBuilder<?> @NotNull... constraint) {
		return this.addColumn(column, null, constraint);
	}

	@Contract(value = "_, _, _ -> this", mutates = "this")
	public @NotNull SQLAlterTableBuilder addColumn(@NotNull String column, @Nullable String type, @Nullable SQLConstraintBuilder<?> @NotNull... constraint) {
		this.addColumn = new SQLCreateTableBuilder.Column(column, type, constraint);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLAlterTableBuilder dropColumn(@NotNull String column) {
		this.dropColumn = column;
		return this;
	}

	@Override
	public @NotNull String build() {
		if (this.table == null)
			throw new IllegalStateException("Cannot alter table without name. You must call .table(name) to specify the table name");
		if (invalidActions(this.renameTable, this.renameColumn, this.addColumn, this.dropColumn))
			throw new IllegalStateException("Cannot perform multiple (or no) actions in the same alter table statement. You must call exactly one of: .renameTable(newName), .renameColumn(column, newName), .addColumn(column, ...) or .dropColumn(column)");

		String action;
		if (this.renameTable != null)
			action = " RENAME TO " + quoteName(this.renameTable);
		else if (this.renameColumn != null && this.newColumnName != null)
			action = " RENAME COLUMN " + quoteName(this.renameColumn) + " TO " + quoteName(this.newColumnName);
		else if (this.addColumn != null)
			action = " ADD COLUMN " + this.addColumn;
		else if (this.dropColumn != null)
			action = " DROP COLUMN " + quoteName(this.dropColumn);
		else
			throw new IllegalStateException("You must choose an action to perform. It must be either .renameTable(newName), .renameColumn(column, newName), .addColumn(column, ...) or .dropColumn(column)");

		return "ALTER TABLE " + quoteName(this.table) + action;
	}

	private static boolean invalidActions(@Nullable Object @NotNull... actions) {
		boolean actionFound = false;
		for (Object action : actions) {
			if (action != null) {
				if (actionFound)
					return true;
				actionFound = true;
			}
		}
		return !actionFound;
	}
}
