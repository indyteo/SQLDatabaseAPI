package fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint;

public class SQLUniqueConstraintBuilder extends SQLKeyUniqueConstraintBuilder<SQLUniqueConstraintBuilder> {
	public SQLUniqueConstraintBuilder() {
		super("UNIQUE");
	}
}
