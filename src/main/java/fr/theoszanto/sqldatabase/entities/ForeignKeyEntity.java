package fr.theoszanto.sqldatabase.entities;

import org.jetbrains.annotations.NotNull;

public class ForeignKeyEntity {
	private final @NotNull ColumnEntity column;
	private final @NotNull ColumnEntity reference;
	private final @NotNull TableEntity table;

	public ForeignKeyEntity(@NotNull ColumnEntity column, @NotNull ColumnEntity reference, @NotNull TableEntity table) {
		this.column = column;
		this.table = table;
		this.reference = reference;
	}

	public @NotNull ColumnEntity getColumn() {
		return this.column;
	}

	public @NotNull ColumnEntity getReference() {
		return this.reference;
	}

	public @NotNull TableEntity getTable() {
		return this.table;
	}
}
