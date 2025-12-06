package com.lexivo.db;

import com.lexivo.schema.EmailConfirmationCodeData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableEmailConfirmationCodes {
	private static final String COL_EMAIL = "email";
	private static final String COL_CODE = "code";
	private static final String COL_CREATED_AT = "created_at";
	private static final String COL_EXPIRES_AT = "expires_at";

	public EmailConfirmationCodeData getByEmail(String email) throws SQLException {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM email_confirmation_codes WHERE email = ?")) {
			statement.setString(1, email);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) return null;

				String eml = resultSet.getString(COL_EMAIL);
				String code = resultSet.getString(COL_CODE);
				long createdAt = resultSet.getLong(COL_CREATED_AT);
				long expiresAt = resultSet.getLong(COL_EXPIRES_AT);

				return new EmailConfirmationCodeData(eml, code, createdAt, expiresAt);
			}
		}
	}

	public boolean addConfirmationCode(EmailConfirmationCodeData confirmationCodeData) throws SQLException {
		final boolean[] success = {false};
		Db.executeTransaction((connection -> {
			try(PreparedStatement statement = connection.prepareStatement("""
					INSERT INTO email_confirmation_codes (email, code, created_at, expires_at)
					VALUES(?,?,?,?)
					ON CONFLICT(email) DO UPDATE SET
					   code = EXCLUDED.code,
					   created_at = EXCLUDED.created_at,
					   expires_at = EXCLUDED.expires_at;
				""")) {

				statement.setString(1, confirmationCodeData.getEmail());
				statement.setString(2, confirmationCodeData.getCode());
				statement.setLong(3, confirmationCodeData.getCreatedAt());
				statement.setLong(4, confirmationCodeData.getExpiresAt());
				success[0] = statement.executeUpdate() > 0;
			}
			catch (SQLException sqle) {
				// TODO: Handle
				System.err.println(sqle.getMessage());
			}
		}));
		return success[0];
	}

	public void deleteWhereEmail(String email) throws SQLException {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM email_confirmation_codes WHERE email = ?")) {
			statement.setString(1, email);
		}
	}
}
