package fr.theoszanto.sqldatabase.sqlbuilders.join;

public class SQLInnerJoinBuilder extends SQLInnerLeftJoinBuilder<SQLInnerJoinBuilder> {
	public SQLInnerJoinBuilder() {
		super("INNER");
	}
}
