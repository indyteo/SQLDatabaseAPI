package fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SQLForeignKeyConstraintBuilder extends SQLKeyUniqueConstraintBuilder<SQLForeignKeyConstraintBuilder> {
	private @Nullable String references;
	private final @NotNull List<@NotNull String> columns = new ArrayList<>();
	private @Nullable KeyModifiedAction onUpdate;
	private @Nullable KeyModifiedAction onDelete;

	public SQLForeignKeyConstraintBuilder() {
		super("FOREIGN KEY");
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLForeignKeyConstraintBuilder references(@NotNull String references) {
		this.references = references;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLForeignKeyConstraintBuilder referencedColumn(@NotNull String column) {
		this.columns.add(column);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLForeignKeyConstraintBuilder onUpdate(@NotNull KeyModifiedAction onUpdate) {
		this.onUpdate = onUpdate;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLForeignKeyConstraintBuilder onDelete(@NotNull KeyModifiedAction onDelete) {
		this.onDelete = onDelete;
		return this;
	}

	@Override
	protected @NotNull String buildConstraint() {
		if (this.references == null)
			throw new IllegalStateException("Missing references table in foreign key constraint definition");

		// References table columns
		String columns = this.columns.isEmpty() ? "" : CollectionsUtils.join(", ", " (", ")", this.columns, SQLBuilder::quoteName);

		// Using update & delete policy
		String onUpdate = this.onUpdate == null ? "" : " ON UPDATE " + this.onUpdate;
		String onDelete = this.onDelete == null ? "" : " ON DELETE " + this.onDelete;

		return super.buildConstraint() + " REFERENCES " + quoteName(this.references) + columns + onUpdate + onDelete;
	}

	public enum KeyModifiedAction {
		SET_NULL, SET_DEFAULT, CASCADE, RESTRICT, NO_ACTION;

		@Override
		public @NotNull String toString() {
			return this.name().replace('_', ' ');
		}
	}
}
