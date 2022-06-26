package fr.theoszanto.sqldatabase.sqlbuilders.dml;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class SQLWOLOARCBuilder<T extends SQLWOLOARCBuilder<T>> extends SQLWOLOBuilder<T> {
	private boolean allRowsConfirm;

	@Contract(value = " -> this", mutates = "this")
	public @NotNull T allRows() {
		return this.allRows(true);
	}

	@SuppressWarnings("unchecked")
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T allRows(boolean allRows) {
		this.allRowsConfirm = allRows;
		return (T) this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.condition == null && !this.allRowsConfirm)
			throw new IllegalStateException("Cannot operate over all rows from table without explicit confirmation by calling .allRows()");
		if (this.condition != null && this.allRowsConfirm)
			throw new IllegalStateException("Cannot operate over all rows with a condition. You must call either .where(condition) or .allRows() but not both");

		return super.build();
	}
}
