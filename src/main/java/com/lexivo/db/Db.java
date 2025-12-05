package com.lexivo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Db {
	private static final DbUser user = new DbUser();
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

	public static DbUser user() {
		return user;
	}
}
