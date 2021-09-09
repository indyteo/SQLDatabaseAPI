package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.Database;
import fr.theoszanto.sqldatabase.annotations.DatabaseExclude;
import fr.theoszanto.sqldatabase.annotations.DatabaseField;
import fr.theoszanto.sqldatabase.annotations.DatabaseForeignKey;
import fr.theoszanto.sqldatabase.annotations.DatabaseModelBinding;
import fr.theoszanto.sqldatabase.annotations.DatabasePrimaryKey;
import fr.theoszanto.sqldatabase.annotations.DatabaseTable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EntitiesFactory {
	private static final @NotNull Map<@NotNull Class<?>, @NotNull TableEntity> tablesRegistry = new HashMap<>();

	private static final int IGNORED_FIELDS_MODIFIERS = Modifier.FINAL | Modifier.TRANSIENT | Modifier.STATIC | Modifier.NATIVE;

	private EntitiesFactory() {}

	public static @NotNull ColumnEntity column(@NotNull TableEntity table, @NotNull Field field) {
		DatabaseField fieldDescriptor = field.getAnnotation(DatabaseField.class);
		String name = fieldDescriptor == null ? field.getName() : fieldDescriptor.value();
		// Verify if column was already created
		if (table.getColumns().containsKey(name))
			return table.getColumns().get(name);
		// Create column
		return new ColumnEntity(table, name, field.getType(), field);
	}

	public static @NotNull TableEntity table(@NotNull Class<?> type) {
		// Verify if table was already created
		if (tablesRegistry.containsKey(type))
			return tablesRegistry.get(type);

		// Ensure descriptor is properly set or define table as model binding
		DatabaseTable tableDescriptor = type.getAnnotation(DatabaseTable.class);
		if (tableDescriptor == null && !type.isAnnotationPresent(DatabaseModelBinding.class))
			throw new IllegalArgumentException("Cannot find neither @DatabaseTable nor @DatabaseModelBinding on " + type);
		String tableName = tableDescriptor == null ? "" : tableDescriptor.value();

		// Create table
		TableEntity table = new TableEntity(tableName, type);
		tablesRegistry.put(type, table);
		List<ColumnEntity> primaryKeyColumns = new ArrayList<>();
		forEachFields(type, field -> {
			// Get column
			ColumnEntity column = column(table, field);
			table.getColumns().put(column.getName(), column);

			// Check primary key
			if (field.isAnnotationPresent(DatabasePrimaryKey.class))
				primaryKeyColumns.add(column);

			// Check foreign key
			if (!Database.shouldTreatDirectly(column.getType())) {
				DatabaseForeignKey foreignKey = field.getAnnotation(DatabaseForeignKey.class);
				if (foreignKey != null) {
					// Retrieve referenced informations
					TableEntity referencedTable = table(column.getType());
					String referenced = foreignKey.value();
					if (referenced.isEmpty())
						referenced = column.getName();
					ColumnEntity referencedColumn = referencedTable.getColumns().get(referenced);
					if (referencedColumn == null)
						throw new IllegalStateException("Cannot find referenced column \"" + referenced + "\" on table " + referencedTable.getName());
					table.getForeignKeys().put(column, new ForeignKeyEntity(column, referencedColumn, referencedTable));
				}
			}
		});
		table.setPrimaryKey(new PrimaryKeyEntity(primaryKeyColumns));
		return table;
	}

	private static void forEachFields(@NotNull Class<?> type, @NotNull Consumer<@NotNull Field> action) {
		for (Field field : type.getDeclaredFields())
			if (!field.isAnnotationPresent(DatabaseExclude.class) && (field.getModifiers() & IGNORED_FIELDS_MODIFIERS) == 0)
				action.accept(field);
		Class<?> superType = type.getSuperclass();
		if (superType != null && superType != Object.class)
			forEachFields(superType, action);
	}
}
