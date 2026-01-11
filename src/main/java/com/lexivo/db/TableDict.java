package com.lexivo.db;

import com.lexivo.logger.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TableDict {
	private static final String COL_ID = "id";
	private static final String COL_LANG = "lang";
	private static final String COL_USER_EMAIL = "user_email";
	private final Logger logger = new Logger();

	public boolean isUserAuthorized(String dictId, String requesterEmail) {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM dict WHERE id = ?")) {
			statement.setString(1, dictId);
			try(ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) return true;
				String userEmail = resultSet.getString(COL_USER_EMAIL);
				return requesterEmail.equals(userEmail);
			}
		}
		catch(Exception e) {
			logger.exception(e, new String[]{"Exception in TableDict.isUserAuthorized", e.getMessage()});
			return false;
		}
	}
}
