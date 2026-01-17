package com.lexivo.db;

import com.lexivo.exceptions.UnauthorizedAccessException;
import com.lexivo.logger.Logger;
import com.lexivo.schema.appschema.Grammar;
import com.lexivo.schema.appschema.GrammarSubmenu;
import com.lexivo.util.ReceivedDataUtil;

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

	public List<Grammar> getAll(String dictId, String userEmail) {
		String sql = "SELECT * FROM grammar WHERE " + COL_DICT_ID + " = ?";
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			String joinedId = ReceivedDataUtil.createJoinedId(userEmail, dictId);
			statement.setString(1, joinedId);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<Grammar> grammarList = new ArrayList<>();

				while (resultSet.next()) {
					String id = resultSet.getString(COL_ID);
					String header = resultSet.getString(COL_HEADER);
					List<GrammarSubmenu> submenuList = submenuTable.getByGrammarId(ReceivedDataUtil.createJoinedId(joinedId, id));

					String[] idList = ReceivedDataUtil.separateJoinedId(id);
					grammarList.add(new Grammar(
							idList[idList.length - 1],
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

	public Grammar getById(String grammarId, String dictId, String userEmail) {
		String sql = "SELECT * FROM grammar WHERE " + COL_ID + " = ?";
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			String joinedId = ReceivedDataUtil.createJoinedId(userEmail, dictId, grammarId);
			statement.setString(1, joinedId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) return null;

				String id = resultSet.getString(COL_ID);
				String header = resultSet.getString(COL_HEADER);
				List<GrammarSubmenu> submenuList = submenuTable.getByGrammarId(joinedId);

				String[] idList = ReceivedDataUtil.separateJoinedId(id);
				return new Grammar(idList[idList.length - 1], header, submenuList);
			}
		}
		catch (Exception e) {
			logger.exception(e, new String[]{"Exception in TableGrammar.getById", e.getMessage()});
			return null;
		}
	}

	public void add(Grammar[] grammarList, String dictId, String userEmail) throws UnauthorizedAccessException {
		String sql = "INSERT INTO grammar ("+
				COL_ID +"," +
				COL_DICT_ID + "," +
				COL_USER_EMAIL + "," +
				COL_HEADER +
				") VALUES(?,?,?,?)";
		try {
			if (!Db.dict().isUserAuthorized(ReceivedDataUtil.createJoinedId(userEmail, dictId), userEmail)) throw new UnauthorizedAccessException();

			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {
					for (var grammar : grammarList) {
						String joinedId = ReceivedDataUtil.createJoinedId(userEmail, dictId, grammar.id);

						int index = 1;
						statement.setString(index++, joinedId);
						statement.setString(index++, dictId);
						statement.setString(index++, userEmail);
						statement.setString(index, grammar.header);
						submenuTable.add(grammar.getSubmenuList(), joinedId, userEmail);

						statement.addBatch();
					}
					statement.executeBatch();
				}
			}));
		}
		catch (Exception e) {
			if (e instanceof UnauthorizedAccessException) throw new UnauthorizedAccessException();
			logger.exception(e, userEmail, new String[]{"Exception in TableGrammar.add"});
		}
	}

	public void update(Grammar grammar, String dictId, String userEmail) throws UnauthorizedAccessException {
		String sql = "UPDATE grammar SET " +
				COL_HEADER + "= ?" +
				" WHERE " + COL_ID + "= ?";
		try {
			String joinedId = ReceivedDataUtil.createJoinedId(userEmail, dictId, grammar.id);
			if (!isUserAuthorized(joinedId, userEmail)) throw new UnauthorizedAccessException();

			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {
					int index = 1;
					statement.setString(index++, grammar.header);
					statement.setString(index, joinedId);
					submenuTable.update(grammar.getSubmenuList(), joinedId);

					statement.execute();
				}
			}));
		}
		catch (Exception e) {
			if (e instanceof UnauthorizedAccessException) throw new UnauthorizedAccessException();
			logger.exception(e, userEmail, new String[]{"Exception in TableGrammar.update"});
		}
	}

	public void delete(String grammarId, String dictId, String userEmail) throws UnauthorizedAccessException {
		try {
			String joinedId = ReceivedDataUtil.createJoinedId(userEmail, dictId, grammarId);
			if (!isUserAuthorized(joinedId, userEmail)) throw new UnauthorizedAccessException();

			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement("DELETE FROM grammar WHERE id = ?")) {
					statement.setString(1, joinedId);
					statement.execute();
				}
			}));
		}
		catch (Exception e) {
			if (e instanceof UnauthorizedAccessException) throw new UnauthorizedAccessException();
			logger.exception(e, userEmail, new String[]{"Exception in TableGrammar.delete"});
		}
	}
}
