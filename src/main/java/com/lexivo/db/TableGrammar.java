package com.lexivo.db;

import com.lexivo.logger.Logger;
import com.lexivo.schema.appschema.Grammar;
import com.lexivo.schema.appschema.GrammarSubmenu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TableGrammar {
	private static final String COL_ID = "id";
	private static final String COL_DICT_ID = "dict_id";
	private static final String COL_USER_EMAIL = "user_email";
	private static final String COL_HEADER = "header";
	private static final TableGrammarSubmenu submenuTable = new TableGrammarSubmenu();
	private final Logger logger = new Logger();

	public boolean isUserAuthorized(String grammarId, String requesterEmail) {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM grammar WHERE id = ?")) {
			statement.setString(1, grammarId);
			try(ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) return true;
				String userEmail = resultSet.getString(COL_USER_EMAIL);
				return requesterEmail.equals(userEmail);
			}
		}
		catch(Exception e) {
			logger.exception(e, new String[]{"Exception in TableGrammar.isUserAuthorized", e.getMessage()});
			return false;
		}
	}

	public List<Grammar> getAll(String dictId) {
		String sql = "SELECT * FROM grammar WHERE " + COL_DICT_ID + " = ?";
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, dictId);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<Grammar> grammarList = new ArrayList<>();

				while (resultSet.next()) {
					String id = resultSet.getString(COL_ID);
					String header = resultSet.getString(COL_HEADER);
					List<GrammarSubmenu> submenuList = submenuTable.getByGrammarId(id);

					grammarList.add(new Grammar(
							id,
							header,
							submenuList
					));
				}

				return grammarList;
			}
		}
		catch (Exception e) {
			logger.exception(e, new String[]{"Exception in TableGrammar.getAll", e.getMessage()});
			return List.of();
		}
	}

//	TODO: Get by ID, Add, Update, Delete
}
