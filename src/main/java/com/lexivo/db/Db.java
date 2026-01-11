package com.lexivo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Db {
	private static final TableUsers users = new TableUsers();
	private static final TableEmailConfirmationCodes emailConfirmationCodes = new TableEmailConfirmationCodes();
	private static final TableLogs logs = new TableLogs();
	private static final TableLang lang = new TableLang();
	private static final TableDict dict = new TableDict();
	private static final TableWord word = new TableWord();
	private static final TableGrammar grammar = new TableGrammar();

	public static Connection getDbConnection() throws SQLException {
		final String url = System.getenv("DB_URL");
		final String user = System.getenv("DB_USER");
		final String password = System.getenv("DB_PASSWORD");

		return DriverManager.getConnection(url, user, password);
	}

	protected static void executeTransaction(TransactionCallback callback) throws SQLException {
		Connection connection = null;
		try {
			connection = getDbConnection();
			connection.setAutoCommit(false);

			callback.run(connection);

			connection.commit();
		}
		catch(SQLException sqle) {
			if (connection != null) connection.rollback();
			throw sqle;
		}
		finally {
			if (connection != null) connection.close();
		}
	}

	public static TableUsers users() {
		return users;
	}

	public static TableEmailConfirmationCodes emailConfirmationCodes() {
		return emailConfirmationCodes;
	}

	public static TableLogs logs() {
		return logs;
	}

	public static TableLang lang() {
		return lang;
	}

	public static TableDict dict() {
		return dict;
	}

	public static TableWord word() {
		return word;
	}
	public static TableGrammar grammar() {
		return grammar;
	}
}
