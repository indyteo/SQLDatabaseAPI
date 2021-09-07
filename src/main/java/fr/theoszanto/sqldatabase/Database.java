package fr.theoszanto.sqldatabase;

import fr.theoszanto.sqldatabase.entities.ColumnEntity;
import fr.theoszanto.sqldatabase.entities.EntitiesFactory;
import fr.theoszanto.sqldatabase.entities.TableEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
	private final File folder;
	private Connection connection;

	public static final char BIND_RECURSION_SEPARATOR = '_';
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_GET_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_LIST_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_SET_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_DELETE_REQUESTS_CACHE = new HashMap<>();

	public Database(@NotNull File folder) {
		this.folder = folder;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void initialize(@NotNull String file) {
		File data = new File(this.folder, file);
		if (!data.exists()) {
			try {
				data.getParentFile().mkdirs();
				data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			if (this.connection != null && !this.connection.isClosed())
				this.connection.close();
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private @NotNull PreparedStatement prepare(@NotNull String sql, @Nullable Object @NotNull... params) {
		try {
			System.out.println(sql);
			for (Object param : params)
				System.out.println(param);
			PreparedStatement statement = this.connection.prepareStatement(sql);
			for (int i = 0; i < params.length; i++)
				statement.setObject(i + 1, params[i]);
			return statement;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public void execute(@NotNull String sql, @Nullable Object @NotNull... params) {
		try {
			PreparedStatement statement = this.prepare(sql, params);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public <T> @Nullable T get(@NotNull Class<T> type, @NotNull Object @NotNull... id) {
		return this.getSql(type, SQL_GET_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlGetQuery), id);
	}

	public <T> @Nullable T getSql(@NotNull Class<T> type, @NotNull String sql, @Nullable Object @NotNull... params) {
		try {
			PreparedStatement statement = this.prepare(sql, params);
			ResultSet result = statement.executeQuery();
			T get = result.next() ? this.bind(type, result) : null;
			statement.close();
			return get;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public <T> @NotNull List<@NotNull T> list(@NotNull Class<T> type) {
		return this.listSql(type, SQL_LIST_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlListQuery));
	}

	public <T> @NotNull List<@NotNull T> listSql(@NotNull Class<T> type, @NotNull String sql, @Nullable Object @NotNull... params) {
		try {
			PreparedStatement statement = this.prepare(sql, params);
			ResultSet result = statement.executeQuery();
			List<T> list = new ArrayList<>();
			while (result.next())
				list.add(this.bind(type, result));
			statement.close();
			return list;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public void set(@NotNull Object value) {
		try {
			this.execute(SQL_SET_REQUESTS_CACHE.computeIfAbsent(value.getClass(), Database::buildSqlSetQuery), explode(value));
		} catch (ReflectiveOperationException e) {
			throw new DatabaseException(e);
		}
	}

	private static @NotNull Object @NotNull[] explode(@NotNull Object value) throws ReflectiveOperationException {
		TableEntity table = EntitiesFactory.table(value.getClass());
		Object[] params = new Object[table.getColumns().size()];
		int i = 0;
		for (ColumnEntity column : table) {
			Field field = column.getField();
			field.setAccessible(true);
			params[i++] = field.get(value);
		}
		return params;
	}

	private <T> @NotNull T bind(@NotNull Class<T> type, @NotNull ResultSet result) {
		return this.bind(type, result, "");
	}

	private <T> @NotNull T bind(@NotNull Class<T> type, @NotNull ResultSet result, @NotNull String prefix) {
		try {
			T object = type.getConstructor().newInstance();
			TableEntity table = EntitiesFactory.table(type);
			for (ColumnEntity column : table) {
				Field field = column.getField();
				field.setAccessible(true);
				String name = prefix + column.getName();
				Class<?> fieldType = column.getType();
				Object value;
				if (shouldTreatDirectly(fieldType))
					value = getResultObject(fieldType, result, name);
				else
					value = this.bind(fieldType, result, name + BIND_RECURSION_SEPARATOR);
				field.set(object, value);
			}
			return object;
		} catch (IllegalStateException | IllegalArgumentException | ReflectiveOperationException | SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public static boolean shouldTreatDirectly(@NotNull Class<?> type) {
		return type.isPrimitive() || type == String.class || type == Date.class;
	}

	private static @Nullable Object getResultObject(@NotNull Class<?> type, @NotNull ResultSet result, @NotNull String name) throws SQLException {
		if (type == boolean.class)
			return result.getBoolean(name);
		if (type == byte.class)
			return result.getByte(name);
		if (type == short.class)
			return result.getShort(name);
		if (type == int.class)
			return result.getInt(name);
		if (type == long.class)
			return result.getLong(name);
		if (type == float.class)
			return result.getFloat(name);
		if (type == double.class)
			return result.getDouble(name);
		if (type == char.class) {
			String str = result.getString(name);
			return str == null ? 0 : str.charAt(0);
		}
		if (type == String.class)
			return result.getString(name);
		if (type == Date.class)
			return result.getDate(name);
		return null;
	}

	private static @NotNull String buildSqlGetQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).select().build();
	}

	private static @NotNull String buildSqlListQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).select().where(null).build();
	}

	private static @NotNull String buildSqlSetQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).upsert().build();
	}

	public boolean isRunning() {
		try {
			return this.connection != null && this.connection.isValid(0);
		} catch (SQLException e) {
			return false;
		}
	}
}
