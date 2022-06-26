package fr.theoszanto.sqldatabase.sqlbuilders.dml.join;

public class SQLLeftJoinBuilder extends SQLInnerLeftJoinBuilder<SQLLeftJoinBuilder> {
	public SQLLeftJoinBuilder() {
		super("LEFT");
	}
}
