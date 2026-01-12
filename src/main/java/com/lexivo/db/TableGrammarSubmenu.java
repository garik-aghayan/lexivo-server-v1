package com.lexivo.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lexivo.logger.Logger;
import com.lexivo.schema.appschema.GrammarSubmenu;
import com.lexivo.util.ReceivedDataUtil;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TableGrammarSubmenu {
	private static final String COL_ID = "id";
	private static final String COL_GRAMMAR_ID = "grammar_id";
	private static final String COL_USER_EMAIL = "user_email";
	private static final String COL_HEADER = "header";
	private static final String COL_EXPLANATIONS_JSON = "explanations_json";
	private static final String COL_EXAMPLES_JSON = "examples_json";
	private final Logger logger = new Logger();
	private final Gson gson = new GsonBuilder().serializeNulls().create();

	public List<GrammarSubmenu> getByGrammarId(String grammarId) {
		String sql = "SELECT * FROM grammar_submenu WHERE " + COL_GRAMMAR_ID + " = ?";
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, grammarId);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<GrammarSubmenu> submenuList = new ArrayList<>();

				while (resultSet.next()) {
					String id = resultSet.getString(COL_ID);

					String header = resultSet.getString(COL_HEADER);
					String explanationsJson = resultSet.getString(COL_EXPLANATIONS_JSON);
					String examplesJson = resultSet.getString(COL_EXAMPLES_JSON);

					Type listType = new TypeToken<ArrayList<String>>(){}.getType();

					List<String> explanations = gson.fromJson(explanationsJson, listType);
					List<String> examples = gson.fromJson(examplesJson, listType);

					submenuList.add(new GrammarSubmenu(
							id,
							header,
							explanations,
							examples
					));
				}

				return submenuList;
			}
		}
		catch (Exception e) {
			logger.exception(e, new String[]{"Exception in TableGrammarSubmenu.getByGrammarId", e.getMessage()});
			return List.of();
		}
	}

	public void add(List<GrammarSubmenu> grammarSubmenuList, String grammarId, String userEmail) {
		String sql = "INSERT INTO grammar_submenu ("+
				COL_ID +"," +
				COL_GRAMMAR_ID + "," +
				COL_USER_EMAIL + "," +
				COL_HEADER + "," +
				COL_EXPLANATIONS_JSON + "," +
				COL_EXAMPLES_JSON +
				") VALUES(?,?,?,?,?,?)";
		try {
			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {
					for (var grammarSubmenu : grammarSubmenuList) {
						int index = 1;
						statement.setString(index++, ReceivedDataUtil.createJoinedId(grammarId, grammarSubmenu.id));
						statement.setString(index++, grammarId);
						statement.setString(index++, userEmail);
						statement.setString(index++, grammarSubmenu.header);
						statement.setString(index++, gson.toJson(grammarSubmenu.getExplanations()));
						statement.setString(index, gson.toJson(grammarSubmenu.getExamples()));

						statement.addBatch();
					}

					statement.executeBatch();
				}
			}));
		}
		catch (Exception e) {
			logger.exception(e, userEmail, new String[]{"Exception in TableGrammarSubmenu.add"});
		}
	}

	public void update(List<GrammarSubmenu> grammarSubmenuList, String grammarId) {
		String sql = "UPDATE grammar_submenu SET " +
				COL_HEADER + "= ?," +
				COL_EXPLANATIONS_JSON + "= ?," +
				COL_EXAMPLES_JSON + "= ?" +
				" WHERE " + COL_ID + "= ?";
		try {
			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {
					for (var grammarSubmenu : grammarSubmenuList) {
						int index = 1;
						statement.setString(index++, grammarSubmenu.header);
						statement.setString(index++, gson.toJson(grammarSubmenu.getExplanations()));
						statement.setString(index++, gson.toJson(grammarSubmenu.getExamples()));
						statement.setString(index, ReceivedDataUtil.createJoinedId(grammarId, grammarSubmenu.id));

						statement.addBatch();
					}

					statement.executeBatch();
				}
			}));
		}
		catch (Exception e) {
			logger.exception(e, new String[]{"Exception in TableGrammarSubmenu.update"});
		}
	}
}
