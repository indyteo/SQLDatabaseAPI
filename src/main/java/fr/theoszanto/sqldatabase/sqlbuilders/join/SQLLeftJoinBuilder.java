package fr.theoszanto.sqldatabase.sqlbuilders.join;

public class SQLLeftJoinBuilder extends SQLInnerLeftJoinBuilder<SQLLeftJoinBuilder> {
	public SQLLeftJoinBuilder() {
		super("LEFT");
	}
}
