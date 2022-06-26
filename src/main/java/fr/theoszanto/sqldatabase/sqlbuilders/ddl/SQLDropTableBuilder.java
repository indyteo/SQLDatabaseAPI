package fr.theoszanto.sqldatabase.sqlbuilders.ddl;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLDropTableBuilder extends SQLBuilder {
	private boolean ifExists = false;
	private @Nullable String table;

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLDropTableBuilder ifExists() {
		return this.ifExists(true);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLDropTableBuilder ifExists(boolean ifExists) {
		this.ifExists = ifExists;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLDropTableBuilder table(@NotNull String table) {
		this.table = table;
		return this;
	}

	@Override
	public @NotNull String build() {
		if (this.table == null)
			throw new IllegalStateException("Cannot drop table without name. You must call .table(name) to specify the table name");

		// Drop only if exists
		String ifExists = this.ifExists ? "IF EXISTS " : "";

		return "DROP TABLE " + ifExists + this.table;
	}
}
