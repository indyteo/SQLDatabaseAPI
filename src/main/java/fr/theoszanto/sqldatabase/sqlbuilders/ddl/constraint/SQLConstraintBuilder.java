package fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint;

import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SQLConstraintBuilder<T extends SQLConstraintBuilder<T>> extends SQLBuilder {
	private @Nullable String name;

	@SuppressWarnings("unchecked")
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull T name(@NotNull String name) {
		this.name = name;
		return (T) this;
	}

	@Override
	public @NotNull String build() {
		String constraint = this.buildConstraint();
		return this.name == null ? constraint : "CONSTRAINT " + this.name + " " + constraint;
	}

	protected abstract @NotNull String buildConstraint();

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLPrimaryKeyConstraintBuilder primaryKey() {
		return new SQLPrimaryKeyConstraintBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLForeignKeyConstraintBuilder foreignKey() {
		return new SQLForeignKeyConstraintBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLUniqueConstraintBuilder unique() {
		return new SQLUniqueConstraintBuilder();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull SQLCheckConstraintBuilder check() {
		return new SQLCheckConstraintBuilder();
	}
}
