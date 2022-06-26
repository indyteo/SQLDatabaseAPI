package fr.theoszanto.sqldatabase.sqlbuilders.dml.join;

public class SQLInnerJoinBuilder extends SQLInnerLeftJoinBuilder<SQLInnerJoinBuilder> {
	public SQLInnerJoinBuilder() {
		super("INNER");
	}
}
