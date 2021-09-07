package fr.theoszanto.sqldatabase;

public class DatabaseException extends RuntimeException {
	public DatabaseException(Throwable cause) {
		super("An error occured with database", cause);
	}
}
