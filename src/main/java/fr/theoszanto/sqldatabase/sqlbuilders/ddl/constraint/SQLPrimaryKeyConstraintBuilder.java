package fr.theoszanto.sqldatabase.sqlbuilders.ddl.constraint;

public class SQLPrimaryKeyConstraintBuilder extends SQLKeyUniqueConstraintBuilder<SQLPrimaryKeyConstraintBuilder> {
	public SQLPrimaryKeyConstraintBuilder() {
		super("PRIMARY KEY");
	}
}
