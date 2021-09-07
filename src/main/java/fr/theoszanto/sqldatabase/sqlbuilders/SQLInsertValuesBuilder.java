package fr.theoszanto.sqldatabase.sqlbuilders;

import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLInsertValuesBuilder extends SQLInsertBuilder<SQLInsertValuesBuilder> {
	private final @NotNull Map<@NotNull String, @NotNull List<@NotNull SQLValue>> values = CollectionsUtils.orderedMap();
	private final @NotNull List<String> onConflict = new ArrayList<>();

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull SQLInsertValuesBuilder value(@NotNull String column, @NotNull SQLValue value) {
		this.values.computeIfAbsent(column, k -> new ArrayList<>()).add(value);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLInsertValuesBuilder onConflict(@NotNull String column) {
		this.onConflict.add(column);
		return this;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String build() {
		int size = 0;
		for (Map.Entry<String, List<SQLValue>> value : this.values.entrySet()) {
			int s = value.getValue().size();
			if (size == 0)
				size = s;
			else if (size != s)
				throw new IllegalStateException("Incoherent count of values");
		}

		String values;
		if (this.values.isEmpty())
			values = " DEFAULT VALUES";
		else {
			List<String> columnsList = new ArrayList<>();
			List<List<String>> valuesLists = new ArrayList<>();
			for (Map.Entry<String, List<SQLValue>> value : this.values.entrySet()) {
				columnsList.add(quoteName(value.getKey()));
				List<SQLValue> vals = value.getValue();
				for (int i = 0; i < vals.size(); i++) {
					List<String> v;
					if (i < valuesLists.size())
						v = valuesLists.get(i);
					else
						valuesLists.add(v = new ArrayList<>());
					v.add(vals.get(i).toString());
				}
			}
			List<String> valuesList = new ArrayList<>();
			for (List<String> vals : valuesLists)
				valuesList.add("(" + String.join(", ", vals) + ")");
			values = " (" + String.join(", ", columnsList) + ") VALUES " + String.join(", ", valuesList);

			if (valuesLists.size() == 1 && !this.onConflict.isEmpty()) {
				List<String> updates = new ArrayList<>();
				for (String column : columnsList)
					updates.add(column + " = excluded." + column);
				values += " ON CONFLICT (" + String.join(", ", this.onConflict) + ") DO UPDATE SET " + String.join(", ", updates);
			}
		}

		return super.build() + values;
	}
}
