package fr.theoszanto.sqldatabase.annotations;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseDefault {
	@Language(value = "SQL", prefix = "CREATE TABLE `table` (`column` DEFAULT (", suffix = "));")
	@NotNull String value();
}
