package fr.theoszanto.sqldatabase.sqlbuilders.dml;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLSortOrder;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class SQLWOLOBuilder<T extends SQLWOLOBuilder<T>> extends SQLBuilder {
	protected @Nullable SQLConditionBuilder condition;
	protected final @NotNull Map<@NotNull String, @NotNull SQLSortOrder> orders = CollectionsUtils.orderedMap();
	protected int limit = -1;
	protected int offset = 0;

	@SuppressWarnings("unchecked")
	@Contract(value = " -> this", pure = true)
	private @NotNull T getThis() {
		return (T) this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T where(@Nullable SQLConditionBuilder condition) {
		this.condition = condition;
		return this.getThis();
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T order(@NotNull String column) {
		return this.order(column, SQLSortOrder.ASC);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull T order(@NotNull String column, @NotNull SQLSortOrder sort) {
		this.orders.put(column, sort);
		return this.getThis();
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T limit(int limit) {
		this.limit = limit;
		return this.getThis();
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T offset(int offset) {
		this.offset = offset;
		return this.getThis();
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		if (this.limit < 0 && this.offset > 0)
			throw new IllegalStateException("Cannot specify an OFFSET without a LIMIT value");

		// Condition where select
		String condition = this.condition == null ? "" : " WHERE " + this.condition;

		// Order select
		String orders = this.orders.isEmpty() ? "" : " ORDER BY " + CollectionsUtils.join(", ", this.orders, (column, sort) -> quoteName(column) + " " + sort.name());

		// Limit select
		String limit = this.limit >= 0 ? " LIMIT " + this.limit : "";

		// Offset select
		String offset = this.offset > 0 ? " OFFSET " + this.offset : "";

		return condition + orders + limit + offset;
	}
}
