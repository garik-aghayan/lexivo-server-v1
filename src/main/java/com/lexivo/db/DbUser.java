package com.lexivo.db;

import com.lexivo.schema.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUser {
	private static final String COL_NAME = "name";
	private static final String COL_EMAIL = "email";
	private static final String COL_PASS_HASH = "password_hash";
	private static final String COL_CONFIRMED = "confirmed";

	public User getByEmail(String email) throws SQLException {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?")) {
			statement.setString(1, email);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) return null;

				String name = resultSet.getString(COL_NAME);
				String eml = resultSet.getString(COL_EMAIL);
				String pass_hash = resultSet.getString(COL_PASS_HASH);
				boolean confirmed = resultSet.getBoolean(COL_CONFIRMED);
				return new User(name, eml, pass_hash, confirmed);
			}
		}
	}

	public boolean addUser(User user) throws SQLException {
		final boolean[] success = {false};
			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement("INSERT INTO users (name, email, password_hash, confirmed) VALUES(?,?,?,?)")) {
					statement.setString(1, user.getName().trim());
					statement.setString(2, user.getEmail());
					statement.setString(3, user.getPasswordHash());
					statement.setBoolean(4, user.isConfirmed());
					success[0] = statement.executeUpdate() > 0;
				}
				catch (SQLException sqle) {
					// TODO: Handle
					System.err.println(sqle.getMessage());
				}
			}));
		return success[0];
	}

	public boolean confirmUser(String email) throws SQLException {
		final boolean[] success = {false};
		Db.executeTransaction((connection -> {
			try(PreparedStatement statement = connection.prepareStatement("UPDATE users SET confirmed = TRUE WHERE email = ?")) {
				statement.setString(1, email);
				success[0] = statement.executeUpdate() > 0;
			}
			catch (SQLException sqle) {
				// TODO: Handle
				System.err.println(sqle.getMessage());
			}
		}));
		return success[0];
	}
}
