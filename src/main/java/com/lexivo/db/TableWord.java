package com.lexivo.db;

import com.lexivo.exceptions.UnauthorizedAccessException;
import com.lexivo.logger.Logger;
import com.lexivo.schema.appschema.Word;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TableWord {
	private static final String COL_ID = "id";
	private static final String COL_DICT_ID = "dict_id";
	private static final String COL_USER_EMAIL = "user_email";
	private static final String COL_TYPE = "type";
	private static final String COL_LEVEL = "level";
	private static final String COL_GENDER = "gender";
	private static final String COL_PRACTICE_COUNTDOWN = "practice_countdown";
	private static final String COL_NATIVE = "native";
	private static final String COL_NATIVE_DETAILS = "native_details";
	private static final String COL_PLURAL = "plural";
	private static final String COL_PAST1 = "past1";
	private static final String COL_PAST2 = "past2";
	private static final String COL_DESC = "description";
	private static final String COL_DESC_DETAILS = "description_details";
	private final Logger logger = new Logger();

	public boolean isUserAuthorized(String wordId, String requesterEmail) {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM word WHERE id = ?")) {
			statement.setString(1, wordId);
			try(ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) return true;
				String userEmail = resultSet.getString(COL_USER_EMAIL);
				return requesterEmail.equals(userEmail);
			}
		}
		catch(Exception e) {
			logger.exception(e, new String[]{"Exception in TableWord.isUserAuthorized", e.getMessage()});
			return false;
		}
	}

	public List<Word> getAll(String dictId) {
		String sql = "SELECT * FROM word WHERE " + COL_DICT_ID + " = ?";
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, dictId);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<Word> words = new ArrayList<>();

				while (resultSet.next()) {
					String id = resultSet.getString(COL_ID);
					String type = resultSet.getString(COL_TYPE);
					String level = resultSet.getString(COL_LEVEL);
					String gender = resultSet.getString(COL_GENDER);
					int practiceCountdown = resultSet.getInt(COL_PRACTICE_COUNTDOWN);
					String ntv = resultSet.getString(COL_NATIVE);
					String ntvDetails = resultSet.getString(COL_NATIVE_DETAILS);
					String plural = resultSet.getString(COL_PLURAL);
					String past1 = resultSet.getString(COL_PAST1);
					String past2 = resultSet.getString(COL_PAST2);
					String desc = resultSet.getString(COL_DESC);
					String descDetails = resultSet.getString(COL_DESC_DETAILS);
					words.add(new Word(
							id,
							type,
							level,
							gender,
							practiceCountdown,
							ntv,
							ntvDetails,
							plural,
							past1,
							past2,
							desc,
							descDetails
					));
				}

				return words;
			}
		}
		catch (Exception e) {
			logger.exception(e, new String[]{"Exception in TableWord.getAll", e.getMessage()});
			return List.of();
		}
	}

	public Word getById(String wordId) {
		String sql = "SELECT * FROM word WHERE " + COL_ID + " = ?";
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, wordId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) return null;

				String id = resultSet.getString(COL_ID);
				String type = resultSet.getString(COL_TYPE);
				String level = resultSet.getString(COL_LEVEL);
				String gender = resultSet.getString(COL_GENDER);
				int practiceCountdown = resultSet.getInt(COL_PRACTICE_COUNTDOWN);
				String ntv = resultSet.getString(COL_NATIVE);
				String ntvDetails = resultSet.getString(COL_NATIVE_DETAILS);
				String plural = resultSet.getString(COL_PLURAL);
				String past1 = resultSet.getString(COL_PAST1);
				String past2 = resultSet.getString(COL_PAST2);
				String desc = resultSet.getString(COL_DESC);
				String descDetails = resultSet.getString(COL_DESC_DETAILS);

				return new Word(
						id,
						type,
						level,
						gender,
						practiceCountdown,
						ntv,
						ntvDetails,
						plural,
						past1,
						past2,
						desc,
						descDetails
				);
			}
		}
		catch (Exception e) {
			logger.exception(e, new String[]{"Exception in TableWord.getById", e.getMessage()});
			return null;
		}
	}

	public void add(Word word, String dictId, String userEmail) throws UnauthorizedAccessException {
		String sql = "INSERT INTO word ("+
					COL_ID +"," +
					COL_DICT_ID + "," +
					COL_USER_EMAIL + "," +
					COL_TYPE + "," +
					COL_LEVEL + "," +
					COL_GENDER + "," +
					COL_PRACTICE_COUNTDOWN + "," +
					COL_NATIVE + "," +
					COL_NATIVE_DETAILS + "," +
					COL_PLURAL + "," +
					COL_PAST1 + "," +
					COL_PAST2 + "," +
					COL_DESC + "," +
					COL_DESC_DETAILS +
				") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			if (!Db.dict().isUserAuthorized(dictId, userEmail)) throw new UnauthorizedAccessException();

			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {
					int index = 1;
					statement.setString(index++, word.id);
					statement.setString(index++, dictId);
					statement.setString(index++, userEmail);
					statement.setString(index++, word.type);
					statement.setString(index++, word.level);
					statement.setString(index++, word.gender);
					statement.setInt(index++, word.practiceCountdown);
					statement.setString(index++, word.ntv);
					statement.setString(index++, word.ntvDetails);
					statement.setString(index++, word.plural);
					statement.setString(index++, word.past1);
					statement.setString(index++, word.past2);
					statement.setString(index++, word.desc);
					statement.setString(index, word.descDetails);
					statement.execute();
				}
			}));
		}
		catch (Exception e) {
			if (e instanceof UnauthorizedAccessException) throw new UnauthorizedAccessException();
			logger.exception(e, userEmail, new String[]{"Exception in TableWord.add"});
		}
	}

	public void update(Word word, String userEmail) throws UnauthorizedAccessException {
		String sql = "UPDATE word SET " +
				COL_TYPE + "= ?," +
				COL_LEVEL + "= ?," +
				COL_GENDER + "= ?," +
				COL_PRACTICE_COUNTDOWN + "= ?," +
				COL_NATIVE + "= ?," +
				COL_NATIVE_DETAILS + "= ?," +
				COL_PLURAL + "= ?," +
				COL_PAST1 + "= ?," +
				COL_PAST2 + "= ?," +
				COL_DESC + "= ?," +
				COL_DESC_DETAILS + "= ?" + word.descDetails +
				" WHERE " + COL_ID + "= ?";
		try {
			if (!isUserAuthorized(word.id, userEmail)) throw new UnauthorizedAccessException();

			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {
					int index = 1;
					statement.setString(index++, word.type);
					statement.setString(index++, word.level);
					statement.setString(index++, word.gender);
					statement.setInt(index++, word.practiceCountdown);
					statement.setString(index++, word.ntv);
					statement.setString(index++, word.ntvDetails);
					statement.setString(index++, word.plural);
					statement.setString(index++, word.past1);
					statement.setString(index++, word.past2);
					statement.setString(index++, word.desc);
					statement.setString(index++, word.descDetails);
					statement.setString(index, word.id);
					statement.execute();
				}
			}));
		}
		catch (Exception e) {
			if (e instanceof UnauthorizedAccessException) throw new UnauthorizedAccessException();
			logger.exception(e, userEmail, new String[]{"Exception in TableWord.update"});
		}
	}

	public void delete(String wordId, String userEmail) throws UnauthorizedAccessException {
		try {
			if (!isUserAuthorized(wordId, userEmail)) throw new UnauthorizedAccessException();

			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement("DELETE FROM word WHERE id = ?")) {
					statement.setString(1, wordId);
					statement.execute();
				}
			}));
		}
		catch (Exception e) {
			if (e instanceof UnauthorizedAccessException) throw new UnauthorizedAccessException();
			logger.exception(e, userEmail, new String[]{"Exception in TableWord.delete"});
		}
	}

	public void updateCountdown(int countdownCount, String wordId, String userEmail) throws UnauthorizedAccessException {
		String sql = "UPDATE word SET " + COL_PRACTICE_COUNTDOWN + " = ? WHERE id = ?";

		try {
			if (!isUserAuthorized(wordId, userEmail)) throw new UnauthorizedAccessException();

			Db.executeTransaction((connection -> {
				try(PreparedStatement statement = connection.prepareStatement(sql)) {
					statement.setInt(1, countdownCount);
					statement.setString(2, wordId);
					statement.execute();
				}
			}));
		}
		catch (Exception e) {
			if (e instanceof UnauthorizedAccessException) throw new UnauthorizedAccessException();
			logger.exception(e, userEmail, new String[]{"Exception in TableWord.updateCountdown"});
		}
	}
}
