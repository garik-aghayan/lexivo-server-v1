package com.lexivo.db;

import com.lexivo.logger.Logger;
import com.lexivo.schema.appschema.Lang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TableLang {
	private static final String COL_NAME = "name";
	private static final String COL_NAME_NATIVE = "name_native";
	private final Logger logger = new Logger();

	public List<Lang> getAll() {
		try (Connection connection = Db.getDbConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM lang")) {
			try (ResultSet resultSet = statement.executeQuery()) {
				List<Lang> langList = new ArrayList<>();

				while (resultSet.next()) {
					String name = resultSet.getString(COL_NAME);
					String nameNative = resultSet.getString(COL_NAME_NATIVE);
					langList.add(new Lang(name, nameNative));
				}

				return langList;
			}
		}
		catch (Exception e) {
			logger.exception(e, new String[]{"Exception in TableLang.getAll", e.getMessage()});
			return List.of();
		}
	}

}
