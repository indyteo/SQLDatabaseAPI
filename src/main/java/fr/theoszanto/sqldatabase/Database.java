package fr.theoszanto.sqldatabase;

import fr.theoszanto.sqldatabase.entities.ColumnEntity;
import fr.theoszanto.sqldatabase.entities.EntitiesFactory;
import fr.theoszanto.sqldatabase.entities.ForeignKeyEntity;
import fr.theoszanto.sqldatabase.entities.IndexEntity;
import fr.theoszanto.sqldatabase.entities.TableEntity;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLConditionBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
	private final @NotNull File folder;
	private @Nullable Connection connection;
	private @Nullable Level logLevel = null;

	public static final char BIND_RECURSION_SEPARATOR = '_';
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_CREATE_TABLE_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_DROP_TABLE_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_GET_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_LIST_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_COUNT_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_ADD_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_SET_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Map<@NotNull Class<?>, @NotNull String> SQL_DELETE_REQUESTS_CACHE = new HashMap<>();
	private static final @NotNull Logger LOGGER = Logger.getLogger(Database.class.getName());

	public Database(@NotNull File folder) {
		this.folder = folder;
	}

	public void initialize(@NotNull String file) {
		try {
			LOGGER.info("Connecting to database...");
			LOGGER.config("Root directory: " + this.folder);
			LOGGER.config("Database file: " + file);
			File data = new File(this.folder, file);
			if (!data.exists()) {
				File parent = data.getParentFile();
				if (!parent.exists()) {
					if (parent.mkdirs())
						LOGGER.info("Created \"" + parent + "\" directory.");
					else
						throw new IOException("Unable to create the database directory \"" + parent + "\".");
				}
				if (data.createNewFile())
					LOGGER.info("Created \"" + data + "\" file.");
				else
					throw new IOException("Unable to create the database file \"" + parent + "\".");
			}
			if (this.connection != null && !this.connection.isClosed())
				this.connection.close();
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + data);
			LOGGER.info("Connected to database!");
		} catch (SQLException | IOException e) {
			LOGGER.log(Level.SEVERE, "Unable to connect to database.", e);
			throw new DatabaseException("A fatal error occurred while connecting to database.", e);
		}
	}

	public void setLogLevel(@Nullable Level logLevel) {
		this.logLevel = logLevel;
	}

	public @Nullable Level getLogLevel() {
		return this.logLevel;
	}

	private @NotNull PreparedStatement prepare(@NotNull String sql, @Nullable Object @NotNull... params) throws DatabaseException {
		if (this.connection == null)
			throw new DatabaseException("Database connection not initialized");
		try {
			if (this.logLevel != null) {
				LOGGER.log(this.logLevel, "SQL request: " + sql);
				LOGGER.log(this.logLevel, "Request params: (" + params.length + ")");
				for (Object param : params)
					LOGGER.log(this.logLevel, "\t" + param);
			}
			PreparedStatement statement = this.connection.prepareStatement(sql);
			for (int i = 0; i < params.length; i++)
				statement.setObject(i + 1, params[i]);
			return statement;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public void execute(@NotNull String sql, @Nullable Object @NotNull... params) throws DatabaseException {
		try (PreparedStatement statement = this.prepare(sql, params)) {
			statement.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public void createTable(@NotNull Class<?> type) throws DatabaseException {
		this.execute(SQL_CREATE_TABLE_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlCreateTableQuery));
	}

	public void dropTable(@NotNull Class<?> type) throws DatabaseException {
		this.execute(SQL_DROP_TABLE_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlDropTableQuery));
	}

	public void regenerateTable(@NotNull Class<?> type) throws DatabaseException {
		this.dropTable(type);
		this.createTable(type);
	}

	public void createIndexes(@NotNull Class<?> type) throws DatabaseException {
		TableEntity table = EntitiesFactory.table(type);
		for (ColumnEntity column : table) {
			IndexEntity index = EntitiesFactory.index(column);
			if (index != null)
				this.execute(index.create().build());
		}
	}

	public void dropIndexes(@NotNull Class<?> type) throws DatabaseException {
		TableEntity table = EntitiesFactory.table(type);
		for (ColumnEntity column : table) {
			IndexEntity index = EntitiesFactory.index(column);
			if (index != null)
				this.execute(index.drop().build());
		}
	}

	public void regenerateIndexes(@NotNull Class<?> type) throws DatabaseException {
		this.dropIndexes(type);
		this.createIndexes(type);
	}

	public void createTableAndIndexes(@NotNull Class<?> type) throws DatabaseException {
		this.createTable(type);
		this.createIndexes(type);
	}

	public void dropTableAndIndexes(@NotNull Class<?> type) throws DatabaseException {
		this.dropIndexes(type);
		this.dropTable(type);
	}

	public void regenerateTableAndIndexes(@NotNull Class<?> type) throws DatabaseException {
		this.dropTableAndIndexes(type);
		this.createTableAndIndexes(type);
	}

	public <T> @Nullable T get(@NotNull Class<T> type, @NotNull Object @NotNull... id) throws DatabaseException {
		return this.getSql(type, SQL_GET_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlGetQuery), id);
	}

	public <T> @Nullable T getWhere(@NotNull Class<T> type, @NotNull SQLConditionBuilder where, @NotNull Object @NotNull... params) throws DatabaseException {
		return this.getSql(type, EntitiesFactory.table(type).select().where(where).build(), params);
	}

	public <T> @Nullable T getSql(@NotNull Class<T> type, @NotNull String sql, @Nullable Object @NotNull... params) throws DatabaseException {
		try (PreparedStatement statement = this.prepare(sql, params)) {
			ResultSet result = statement.executeQuery();
			return result.next() ? this.bind(type, result) : null;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> @Nullable T getValue(@NotNull Class<T> type, @NotNull String sql, @Nullable Object @NotNull... params) throws DatabaseException {
		try (PreparedStatement statement = this.prepare(sql, params)) {
			ResultSet result = statement.executeQuery();
			return result.next() ? (T) getResultObject(type, result, result.getMetaData().getColumnName(1)) : null;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Contract("_, !null, _, _ -> !null")
	public <T> @Nullable T getValueOrDefault(@NotNull Class<T> type, @Nullable T def, @NotNull String sql, @Nullable Object @NotNull... params) {
		try {
			T val = this.getValue(type, sql, params);
			return val == null ? def : val;
		} catch (DatabaseException e) {
			return def;
		}
	}

	public <T> @NotNull List<@NotNull T> list(@NotNull Class<T> type) throws DatabaseException {
		return this.listSql(type, SQL_LIST_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlListQuery));
	}

	public <T> @NotNull List<@NotNull T> listWhere(@NotNull Class<T> type, @Nullable SQLConditionBuilder where, @NotNull Object @NotNull... params) throws DatabaseException {
		return this.listSql(type, EntitiesFactory.table(type).select().where(where).build(), params);
	}

	public <T> @NotNull List<@NotNull T> listSql(@NotNull Class<T> type, @NotNull String sql, @Nullable Object @NotNull... params) throws DatabaseException {
		try (PreparedStatement statement = this.prepare(sql, params)) {
			ResultSet result = statement.executeQuery();
			List<T> list = new ArrayList<>();
			while (result.next())
				list.add(this.bind(type, result));
			return list;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public int count(@NotNull Class<?> type) throws DatabaseException {
		return this.getValueOrDefault(int.class, -1, SQL_COUNT_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlCountQuery));
	}

	public int countWhere(@NotNull Class<?> type, @Nullable SQLConditionBuilder where, @NotNull Object @NotNull... params) throws DatabaseException {
		return this.getValueOrDefault(int.class, -1, SQLBuilder.select().from(EntitiesFactory.table(type).getName()).value(SQLValue.COUNT_ALL).where(where).build(), params);
	}

	public int add(@NotNull Object value) throws DatabaseException {
		try {
			this.execute(SQL_ADD_REQUESTS_CACHE.computeIfAbsent(value.getClass(), Database::buildSqlAddQuery), explode(value, false));
			return this.getValueOrDefault(int.class, -1, "SELECT last_insert_rowid() AS id");
		} catch (ReflectiveOperationException e) {
			throw new DatabaseException(e);
		}
	}

	public void set(@NotNull Object value) throws DatabaseException {
		try {
			this.execute(SQL_SET_REQUESTS_CACHE.computeIfAbsent(value.getClass(), Database::buildSqlSetQuery), explode(value, true));
		} catch (ReflectiveOperationException e) {
			throw new DatabaseException(e);
		}
	}

	private static @NotNull Object @NotNull[] explode(@NotNull Object value, boolean includePrimaries) throws ReflectiveOperationException {
		TableEntity table = EntitiesFactory.table(value.getClass());
		int size = table.size();
		if (!includePrimaries)
			size -= table.getPrimaryKey().size();
		Object[] params = new Object[size];
		int i = 0;
		for (ColumnEntity column : table) {
			if (includePrimaries || !column.isPrimary()) {
				Field field = column.getField();
				field.setAccessible(true);
				Object obj = field.get(value);
				if (column.isForeign()) {
					ForeignKeyEntity foreignKey = table.getForeignKeys().get(column);
					if (foreignKey != null && foreignKey.isDeepFetch()) {
						Field foreignField = foreignKey.getReference().getField();
						foreignField.setAccessible(true);
						obj = foreignField.get(obj);
					}
				}
				params[i++] = obj;
			}
		}
		return params;
	}

	public boolean delete(@NotNull Class<?> type, @NotNull Object @NotNull... id) throws DatabaseException {
		this.execute(SQL_DELETE_REQUESTS_CACHE.computeIfAbsent(type, Database::buildSqlDeleteQuery), id);
		return this.getValueOrDefault(int.class, 0, "SELECT changes() AS count") > 0;
	}

	private <T> @NotNull T bind(@NotNull Class<T> type, @NotNull ResultSet result) throws DatabaseException {
		return this.bind(type, result, "");
	}

	private <T> @NotNull T bind(@NotNull Class<T> type, @NotNull ResultSet result, @NotNull String prefix) throws DatabaseException {
		try {
			T object = type.getConstructor().newInstance();
			TableEntity table = EntitiesFactory.table(type);
			for (ColumnEntity column : table) {
				Field field = column.getField();
				field.setAccessible(true);
				String name = prefix + column.getName();
				try {
					result.findColumn(name);
				} catch (SQLException e) {
					if (this.logLevel != null)
						LOGGER.log(this.logLevel, "Ignoring column: " + name + " (not found in result set)");
					continue;
				}
				Class<?> fieldType = column.getType();
				Object value;
				if (column.isForeign() && table.getForeignKeys().get(column).isDeepFetch())
					value = this.bind(fieldType, result, name + BIND_RECURSION_SEPARATOR);
				else {
					value = getResultObject(fieldType, result, name);
					if (result.wasNull() && !fieldType.isPrimitive())
						value = null;
				}
				field.set(object, value);
			}
			return object;
		} catch (IllegalStateException | IllegalArgumentException | ReflectiveOperationException | SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private static @Nullable Object getResultObject(@NotNull Class<?> type, @NotNull ResultSet result, @NotNull String name) throws SQLException {
		if (type == void.class || type == Void.class)
			return null;
		if (type == boolean.class || type == Boolean.class)
			return result.getBoolean(name);
		if (type == byte.class || type == Byte.class)
			return result.getByte(name);
		if (type == short.class || type == Short.class)
			return result.getShort(name);
		if (type == int.class || type == Integer.class)
			return result.getInt(name);
		if (type == long.class || type == Long.class)
			return result.getLong(name);
		if (type == float.class || type == Float.class)
			return result.getFloat(name);
		if (type == double.class || type == Double.class)
			return result.getDouble(name);
		if (type == char.class || type == Character.class) {
			String str = result.getString(name);
			return str == null || str.isEmpty() ? 0 : str.charAt(0);
		}
		if (type == String.class)
			return result.getString(name);
		if (type == Date.class)
			return result.getDate(name);
		if (type == Time.class)
			return result.getTime(name);
		if (type == Timestamp.class) {
			try {
				return result.getTimestamp(name);
			} catch (SQLException e) {
				return Timestamp.from(Instant.ofEpochMilli(result.getLong(name)));
			}
		}
		if (type == BigDecimal.class)
			return result.getBigDecimal(name);
		try {
			return result.getObject(name, type);
		} catch (SQLException e) {
			return null;
		}
	}

	private static @NotNull String buildSqlCreateTableQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).create().build();
	}

	private static @NotNull String buildSqlDropTableQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).drop().build();
	}

	private static @NotNull String buildSqlGetQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).select(true).build();
	}

	private static @NotNull String buildSqlListQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).select().build();
	}

	private static @NotNull String buildSqlCountQuery(@NotNull Class<?> type) {
		return SQLBuilder.select().from(EntitiesFactory.table(type).getName()).value(SQLValue.COUNT_ALL).build();
	}

	private static @NotNull String buildSqlAddQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).insert().build();
	}

	private static @NotNull String buildSqlSetQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).upsert().build();
	}

	private static @NotNull String buildSqlDeleteQuery(@NotNull Class<?> type) {
		return EntitiesFactory.table(type).delete().build();
	}

	public boolean isRunning() {
		try {
			return this.connection != null && this.connection.isValid(0);
		} catch (SQLException e) {
			return false;
		}
	}
}
