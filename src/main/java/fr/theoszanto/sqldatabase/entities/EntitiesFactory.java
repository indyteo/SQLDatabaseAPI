package fr.theoszanto.sqldatabase.entities;

import fr.theoszanto.sqldatabase.annotations.DatabaseExclude;
import fr.theoszanto.sqldatabase.annotations.DatabaseField;
import fr.theoszanto.sqldatabase.annotations.DatabaseForeignKey;
import fr.theoszanto.sqldatabase.annotations.DatabaseIndex;
import fr.theoszanto.sqldatabase.annotations.DatabaseModelBinding;
import fr.theoszanto.sqldatabase.annotations.DatabasePrimaryKey;
import fr.theoszanto.sqldatabase.annotations.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EntitiesFactory {
	private static final @NotNull Map<@NotNull Class<?>, @NotNull TableEntity> tablesRegistry = new HashMap<>();
	private static final @NotNull Map<@NotNull ColumnEntity, @NotNull IndexEntity> indexesRegistry = new HashMap<>();

	private static final int IGNORED_FIELDS_MODIFIERS = Modifier.FINAL | Modifier.TRANSIENT | Modifier.STATIC | Modifier.NATIVE;

	private EntitiesFactory() {}

	public static @NotNull ColumnEntity column(@NotNull TableEntity table, @NotNull Field field) {
		DatabaseField fieldDescriptor = field.getAnnotation(DatabaseField.class);
		String name = fieldDescriptor == null ? field.getName() : fieldDescriptor.value();
		// Verify if column was already created
		if (table.getColumns().containsKey(name))
			return table.getColumns().get(name);
		// Create column
		return new ColumnEntity(table, name, field.getType(), field, field.isAnnotationPresent(DatabasePrimaryKey.class));
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
		AtomicBoolean autoIncrementPrimaryKey = new AtomicBoolean(false);
		forEachFields(type, field -> {
			// Get column
			ColumnEntity column = column(table, field);
			table.addColumn(column);

			// Check primary key
			if (column.isPrimary()) {
				primaryKeyColumns.add(column);
				autoIncrementPrimaryKey.set(autoIncrementPrimaryKey.get() || field.getAnnotation(DatabasePrimaryKey.class).autoIncrement());
			}

			// Check foreign key
			if (column.isForeign()) {
				DatabaseForeignKey foreignKey = field.getAnnotation(DatabaseForeignKey.class);
				if (foreignKey != null) {
					// Retrieve referenced information
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
		if (autoIncrementPrimaryKey.get()) {
			if (primaryKeyColumns.size() != 1)
				throw new IllegalStateException("Cannot create multi-column auto-increment primary key: " + tableName);
			if (invalidAutoIncrementType(primaryKeyColumns.get(0).getType()))
				throw new IllegalStateException();
		}
		table.setPrimaryKey(new PrimaryKeyEntity(autoIncrementPrimaryKey.get(), primaryKeyColumns));
		return table;
	}

	public static @Nullable IndexEntity index(@NotNull ColumnEntity column) {
		if (indexesRegistry.containsKey(column))
			return indexesRegistry.get(column);
		DatabaseIndex databaseIndex = column.getField().getAnnotation(DatabaseIndex.class);
		if (databaseIndex == null)
			return null;
		IndexEntity index = new IndexEntity(column, databaseIndex.unique());
		indexesRegistry.put(column, index);
		return index;
	}

	private static void forEachFields(@NotNull Class<?> type, @NotNull Consumer<@NotNull Field> action) {
		for (Field field : type.getDeclaredFields())
			if (!field.isAnnotationPresent(DatabaseExclude.class) && (field.getModifiers() & IGNORED_FIELDS_MODIFIERS) == 0)
				action.accept(field);
		Class<?> superType = type.getSuperclass();
		if (superType != null && superType != Object.class)
			forEachFields(superType, action);
	}

	private static boolean invalidAutoIncrementType(@NotNull Class<?> type) {
		return type != byte.class && type != Byte.class
				&& type != short.class && type != Short.class
				&& type != int.class && type != Integer.class
				&& type != long.class && type != Long.class
				&& type != BigInteger.class && type != BigDecimal.class;
	}
}
