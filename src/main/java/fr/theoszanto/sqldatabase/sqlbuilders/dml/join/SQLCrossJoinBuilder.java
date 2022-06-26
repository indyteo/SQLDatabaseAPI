package fr.theoszanto.sqldatabase.sqlbuilders.dml.join;

public class SQLCrossJoinBuilder extends SQLJoinBuilder<SQLCrossJoinBuilder> {
	public SQLCrossJoinBuilder() {
		super("CROSS");
	}
}
