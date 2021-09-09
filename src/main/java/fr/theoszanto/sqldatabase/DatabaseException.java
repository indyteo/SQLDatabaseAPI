package fr.theoszanto.sqldatabase;

public class DatabaseException extends RuntimeException {
	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseException(Throwable cause) {
		super("An error occured with database", cause);
	}
}
