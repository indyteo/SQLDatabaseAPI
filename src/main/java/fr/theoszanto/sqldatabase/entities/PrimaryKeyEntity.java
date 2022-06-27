package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLConditionBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PrimaryKeyEntity implements Iterable<@NotNull ColumnEntity> {
	private final boolean autoIncrement;
	private final @NotNull Map<@NotNull String, @NotNull ColumnEntity> columns = CollectionsUtils.orderedMap();

	public PrimaryKeyEntity(boolean autoIncrement, @NotNull ColumnEntity @NotNull... columns) {
		this.autoIncrement = autoIncrement && columns.length == 1;
		for (ColumnEntity column : columns)
			this.columns.put(column.getName(), column);
	}

	public PrimaryKeyEntity(boolean autoIncrement, @NotNull List<@NotNull ColumnEntity> columns) {
		this.autoIncrement = autoIncrement && columns.size() == 1;
		for (ColumnEntity column : columns)
			this.columns.put(column.getName(), column);
	}

	public boolean isAutoIncrement() {
		return this.autoIncrement;
	}

	public @NotNull Map<@NotNull String, @NotNull ColumnEntity> getColumns() {
		return this.columns;
	}

	public int size() {
		return this.columns.size();
	}

	@Override
	public @NotNull Iterator<@NotNull ColumnEntity> iterator() {
		return this.columns.values().iterator();
	}

	public @Nullable SQLConditionBuilder condition() {
		Iterator<ColumnEntity> i = this.iterator();
		if (!i.hasNext())
			return null;
		SQLConditionBuilder builder = conditionEquals(i);
		while (i.hasNext())
			builder.and(conditionEquals(i));
		return builder;
	}

	private static @NotNull SQLConditionBuilder conditionEquals(@NotNull Iterator<@NotNull ColumnEntity> i) {
		return SQLConditionBuilder.equals(i.next().asSQLValue(), SQLValue.PLACEHOLDER);
	}
}
