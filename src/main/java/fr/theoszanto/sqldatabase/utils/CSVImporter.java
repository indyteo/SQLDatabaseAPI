package fr.theoszanto.sqldatabase.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.RFC4180Parser;
import com.opencsv.exceptions.CsvRuntimeException;
import fr.theoszanto.sqldatabase.Database;
import fr.theoszanto.sqldatabase.DatabaseException;
import fr.theoszanto.sqldatabase.entities.EntitiesFactory;
import fr.theoszanto.sqldatabase.entities.TableEntity;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLBuilder;
import fr.theoszanto.sqldatabase.sqlbuilders.SQLValue;
import fr.theoszanto.sqldatabase.sqlbuilders.dml.SQLInsertValuesBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

public class CSVImporter {
	private final @NotNull Database database;
	private int insertSize = 100;
	private @Nullable String nullValue = null;
	private boolean columnsFromCSV = false;
	private boolean createTable = false;
	private @NotNull ICSVParser parser = new RFC4180Parser();

	public CSVImporter(@NotNull Database database) {
		this.database = database;
	}

	@Contract(pure = true)
	public @NotNull Database getDatabase() {
		return this.database;
	}

	@Contract(pure = true)
	public int getInsertSize() {
		return this.insertSize;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull CSVImporter setInsertSize(int insertSize) {
		if (insertSize <= 0)
			throw new IllegalArgumentException("Invalid import block size");
		this.insertSize = insertSize;
		return this;
	}

	@Contract(pure = true)
	public @Nullable String getNullValue() {
		return this.nullValue;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull CSVImporter setNullValue(@Nullable String nullValue) {
		this.nullValue = nullValue;
		return this;
	}

	@Contract(pure = true)
	public boolean isColumnsFromCSV() {
		return this.columnsFromCSV;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull CSVImporter setColumnsFromCSV(boolean columnsFromCSV) {
		this.columnsFromCSV = columnsFromCSV;
		return this;
	}

	@Contract(pure = true)
	public boolean isCreateTable() {
		return this.createTable;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull CSVImporter setCreateTable(boolean createTable) {
		this.createTable = createTable;
		return this;
	}

	@Contract(pure = true)
	public @NotNull ICSVParser getParser() {
		return this.parser;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull CSVImporter setParser(@NotNull ICSVParser parser) {
		this.parser = parser;
		return this;
	}

	public @NotNull Duration importData(@NotNull Class<?> type, @NotNull Path csv) {
		Instant start = Instant.now();
		if (this.createTable)
			this.database.createTable(type);
		TableEntity table = EntitiesFactory.table(type);
		try (Reader reader = Files.newBufferedReader(csv); CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(this.parser).build()) {
			String[] columns;
			Iterator<String[]> iterator = csvReader.iterator();
			if (this.columnsFromCSV) {
				if (!iterator.hasNext())
					throw new CsvRuntimeException("Missing CSV headers");
				columns = iterator.next();
			} else
				columns = table.getColumns().keySet().toArray(new String[0]);
			while (iterator.hasNext()) {
				SQLInsertValuesBuilder insert = SQLBuilder.insertValues().into(table.getName());
				int n = 0;
				while (n < this.insertSize && iterator.hasNext()) {
					String[] row = iterator.next();
					for (int i = 0; i < columns.length; i++) {
						String value = row[i];
						insert.value(columns[i], value.equals(this.nullValue) ? SQLValue.NULL : SQLValue.quoted(value));
					}
					n++;
				}
				this.database.execute(insert.build());
			}
		} catch (IOException | RuntimeException e) { // RuntimeException to catch CsvException from .iterator()
			throw new DatabaseException("Unable to import data from CSV", e);
		}
		Instant end = Instant.now();
		return Duration.between(start, end);
	}
}
