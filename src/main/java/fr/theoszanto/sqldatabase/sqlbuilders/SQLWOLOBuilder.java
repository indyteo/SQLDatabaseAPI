package fr.theoszanto.sqldatabase.sqlbuilders;

import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SQLWOLOBuilder<T extends SQLWOLOBuilder<T>> extends SQLBuilder {
	protected @Nullable SQLConditionBuilder condition;
	protected final @NotNull List<@NotNull OrderEntry> orders = new ArrayList<>();
	protected int limit = 0;
	protected int offset = -1;

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
		return this.order(column, SortOrder.ASC);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull T order(@NotNull String column, @NotNull SortOrder sort) {
		this.orders.add(new OrderEntry(column, sort));
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
		// Condition where select
		String condition = this.condition == null ? "" : " WHERE " + this.condition;

		// Order select
		String orders = this.orders.isEmpty() ? "" : " ORDER BY " + CollectionsUtils.join(", ", this.orders);

		// Limit select
		String limit = this.limit > 0 ? " LIMIT " + this.limit : "";

		// Offset select
		String offset = this.offset >= 0 ? " OFFSET " + this.offset : "";

		return condition + orders + limit + offset;
	}

	public enum SortOrder {
		ASC, DESC
	}

	static class OrderEntry {
		private final @NotNull String column;
		private final @NotNull SortOrder sort;

		public OrderEntry(@NotNull String column, @NotNull SortOrder sort) {
			this.column = column;
			this.sort = sort;
		}

		@Override
		@Contract(value = " -> new", pure = true)
		public @NotNull String toString() {
			return quoteName(this.column) + " " + this.sort.name();
		}
	}
}
