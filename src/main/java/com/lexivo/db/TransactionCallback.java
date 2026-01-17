package com.lexivo.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface TransactionCallback {
	void run(Connection connection) throws SQLException;
}
