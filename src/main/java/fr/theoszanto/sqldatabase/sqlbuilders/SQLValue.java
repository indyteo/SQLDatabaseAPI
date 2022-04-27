package fr.theoszanto.sqldatabase.sqlbuilders;

import fr.theoszanto.sqldatabase.utils.CollectionsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLValue {
	private final @NotNull StringBuilder value;

	public static final @NotNull SQLValue PLACEHOLDER = new SQLValue("?");
	public static final @NotNull SQLValue NULL = new SQLValue("NULL");

	private SQLValue(@NotNull String value) {
		this.value = new StringBuilder(value.length()).append(value);
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	private @NotNull SQLValue operator(@NotNull String operator, @NotNull SQLValue value) {
		this.value.insert(0, '(').append(") ").append(operator).append(" (").append(value).append(')');
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue add(@NotNull SQLValue value) {
		return this.operator("+", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue subtract(@NotNull SQLValue value) {
		return this.operator("-", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue multiply(@NotNull SQLValue value) {
		return this.operator("*", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue divide(@NotNull SQLValue value) {
		return this.operator("/", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue mod(@NotNull SQLValue value) {
		return this.operator("%", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue concat(@NotNull SQLValue value) {
		return this.operator("||", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue binaryAnd(@NotNull SQLValue value) {
		return this.operator("&", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue binaryOr(@NotNull SQLValue value) {
		return this.operator("|", value);
	}

	@Contract(value = " -> this", mutates = "this")
	public @NotNull SQLValue binaryNot() {
		this.value.insert(0, "~(").append(')');
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue binaryLeftShift(@NotNull SQLValue value) {
		return this.operator("<<", value);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull SQLValue binaryRightShift(@NotNull SQLValue value) {
		return this.operator(">>", value);
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return this.value.toString();
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull SQLValue unquoted(@NotNull String value) {
		return new SQLValue(value);
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull SQLValue quoted(@Nullable Object value) {
		return new SQLValue(SQLBuilder.quoteVal(value));
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull SQLValue column(@NotNull String name) {
		return column(null, name);
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLValue column(@Nullable String prefix, @NotNull String name) {
		String quotedPrefix = prefix == null ? "" : SQLBuilder.quoteName(prefix) + ".";
		return new SQLValue(quotedPrefix + SQLBuilder.quoteName(name));
	}

	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull SQLValue function(@NotNull String name, @NotNull SQLValue @NotNull... arguments) {
		return new SQLValue(name + "(" + CollectionsUtils.join(", ", arguments) + ")");
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull SQLValue select(@NotNull SQLSelectBuilder select) {
		return new SQLValue("(" + select + ")");
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull SQLValue list(@NotNull SQLValue @NotNull... values) {
		return function("", values);
	}
}
