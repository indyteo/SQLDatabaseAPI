package fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SQLKeyUniqueConstraintBuilder<T extends SQLKeyUniqueConstraintBuilder<T>> extends SQLConstraintBuilder<T> {
	private final @NotNull String prefix;
	private final @NotNull List<@NotNull String> columns = new ArrayList<>();

	public SQLKeyUniqueConstraintBuilder(@NotNull String prefix) {
		this.prefix = prefix;
	}

	@SuppressWarnings("unchecked")
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T column(@NotNull String column) {
		this.columns.add(column);
		return (T) this;
	}

	@Override
	protected @NotNull String buildConstraint() {
		return this.prefix + CollectionsUtils.join(", ", " (", ")", this.columns, SQLBuilder::quoteName);
	}
}
