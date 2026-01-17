package com.lexivo.db;

import com.lexivo.logger.Logger;
import com.lexivo.schema.EmailConfirmationCodeData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TableEmailConfirmationCodes {
	private static final String COL_EMAIL = "email";
	private static final String COL_CODE = "code";
	private static final String COL_CREATED_AT = "created_at";
	private static final String COL_EXPIRES_AT = "expires_at";
	private final Logger logger = new Logger();

	public EmailConfirmationCodeData getByEmail(String email) {
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
		} catch (Exception e) {
			logger.exception(e, email, new String[]{"Exception in TableEmailConfirmationCodes.getByEmail", e.getMessage()});
			return null;
		}
	}

	public boolean addConfirmationCode(EmailConfirmationCodeData confirmationCodeData) {
		final boolean[] success = {false};
		String sql =
				"""
					INSERT INTO email_confirmation_codes (email, code, created_at, expires_at)
					VALUES(?,?,?,?)
					ON CONFLICT(email) DO UPDATE SET
					   code = EXCLUDED.code,
					   created_at = EXCLUDED.created_at,
					   expires_at = EXCLUDED.expires_at;
				""";
		try {
			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {

					statement.setString(1, confirmationCodeData.getEmail());
					statement.setString(2, confirmationCodeData.getCode());
					statement.setLong(3, confirmationCodeData.getCreatedAt());
					statement.setLong(4, confirmationCodeData.getExpiresAt());
					success[0] = statement.executeUpdate() > 0;
				}
			}));
			return success[0];
		}
		catch (Exception e) {
			logger.exception(e, confirmationCodeData.getEmail(), new String[]{"Exception in TableEmailConfirmationCodes.addConfirmationCode", e.getMessage()});
			return false;
		}
	}

	public void deleteWhereEmail(String email) {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM email_confirmation_codes WHERE email = ?")) {
			statement.setString(1, email);
			statement.execute();
		} catch (Exception e) {
			logger.exception(e, email, new String[]{"Exception in TableEmailConfirmationCodes.deleteWhereEmail", e.getMessage()});
		}
	}
}
