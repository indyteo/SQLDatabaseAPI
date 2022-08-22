package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLCreateIndexBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.ddl.SQLDropIndexBuilder;
import org.jetbrains.annotations.NotNull;

public class IndexEntity {
	private final @NotNull ColumnEntity column;
	private final @NotNull String name;
	private final boolean unique;

	/* package-private */ IndexEntity(@NotNull ColumnEntity column, boolean unique) {
		this.column = column;
		this.name = "idx" + (unique ? "u" : "") + "_" + column.getTable().getName() + "_" + column.getName();
		this.unique = unique;
	}

	public @NotNull ColumnEntity getColumn() {
		return this.column;
	}

	public @NotNull String getName() {
		return this.name;
	}

	public boolean isUnique() {
		return this.unique;
	}

	public @NotNull SQLCreateIndexBuilder create() {
		return SQLBuilder.createIndex().ifNotExists()
				.unique(this.unique)
				.index(this.name)
				.on(this.column.getTable().getName())
				.column(this.column.getName());
	}

	public @NotNull SQLDropIndexBuilder drop() {
		return SQLBuilder.dropIndex().ifExists().index(this.name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IndexEntity column = (IndexEntity) o;
		return this.column.equals(column.column);
	}

	@Override
	public int hashCode() {
		return this.column.hashCode();
	}
}
