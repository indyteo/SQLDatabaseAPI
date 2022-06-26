package fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint;

import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLConditionBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLCheckConstraintBuilder extends SQLConstraintBuilder<SQLCheckConstraintBuilder> {
	private @Nullable SQLConditionBuilder condition;

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLCheckConstraintBuilder name(@NotNull SQLConditionBuilder condition) {
		this.condition = condition;
		return this;
	}

	@Override
	protected @NotNull String buildConstraint() {
		if (this.condition == null)
			throw new IllegalStateException("Missing condition in check constraint definition");
		return "CHECK (" + this.condition + ")";
	}
}
