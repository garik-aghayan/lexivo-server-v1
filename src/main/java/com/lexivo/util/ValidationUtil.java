package com.lexivo.util;

import com.lexivo.logger.Logger;

import java.util.LinkedList;
import java.util.List;

public abstract class ValidationUtil {
	private static final Logger logger = new Logger();
	private static final int NAME_LENGTH = 50;
	public static final String[] PASSWORD_REQUIREMENTS = {"Password must have", "8-32 characters", "at least one upper case letter", "at least on lower case letter", "at least one number"};
	public static final String[] NAME_REQUIREMENTS = {"Name must have", "1-" + NAME_LENGTH + " characters", "only ' '(empty space) characters are not allowed"};

	public static boolean isPasswordValid(String password) {
		return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,32}$");
	}

	public static boolean isNameValid(String name) {
		name = name.trim();
		return !name.isEmpty() && name.length() <= NAME_LENGTH;
	}

	public static String[] getMissingStrings(String[] values, List<String> errorMessages) {
		if (values.length != errorMessages.size()) {
			logger.exception("values.length != errorMessages.size()", "values.length = " + values.length, "errorMessages.size() = " + errorMessages.size());
			throw new IllegalArgumentException("values.size() != errorMessages.size()");
		}
		List<String> missingValues = new LinkedList<>();
		for (int i = 0; i < values.length; i++) {
			var value = values[i];
			if (value == null || value.isBlank()) {
				missingValues.add(errorMessages.get(i));
			}
		}
		return missingValues.toArray(String[]::new);
	}
}
