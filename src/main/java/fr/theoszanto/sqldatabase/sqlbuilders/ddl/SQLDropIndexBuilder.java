package fr.theoszanto.sqldatabase.sqlbuilders.ddl;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLDropIndexBuilder extends SQLBuilder {
	private boolean ifExists = false;
	private @Nullable String index;

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLDropIndexBuilder ifExists() {
		return this.ifExists(true);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLDropIndexBuilder ifExists(boolean ifExists) {
		this.ifExists = ifExists;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLDropIndexBuilder index(@NotNull String index) {
		this.index = index;
		return this;
	}

	@Override
	public @NotNull String build() {
		if (this.index == null)
			throw new IllegalStateException("Cannot drop index without name. You must call .index(name) to specify the index name");

		// Drop only if exists
		String ifExists = this.ifExists ? "IF EXISTS " : "";

		return "DROP INDEX " + ifExists + quoteName(this.index);
	}
}
