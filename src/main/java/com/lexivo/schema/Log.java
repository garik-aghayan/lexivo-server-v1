package com.lexivo.schema;

import com.lexivo.db.Db;
import com.lexivo.exceptions.InvalidLogCategoryException;
import com.lexivo.util.DateAndTime;
import com.lexivo.util.Email;

import java.util.Arrays;
import java.util.List;

public class Log {
	private final long createdAt;
	private final Category category;
	private final List<String> messages;
	private final String userEmail;

	public Log(long createdAt, Category category, String userEmail, List<String> messages) {
		this.createdAt = createdAt;
		this.category = category;
		this.messages = messages;
		this.userEmail = userEmail;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public Category getCategory() {
		return category;
	}

	public String[] getMessages() {
		return messages.toArray(new String[0]);
	}

	public String getUserEmail() {
		return userEmail;
	}

	private static void createLog(Category category, String userEmail, List<String> messages) {
		Log log = new Log(System.currentTimeMillis(), category, userEmail, messages);
		printInConsole(log);
		if (category == Category.EXCEPTION){
			Email.sendEmailToAdmin("Lexivo server exception", "Exception thrown at " + DateAndTime.getFormattedDateAndTimeFromMs(log.createdAt) + ".\nUser related: " + (userEmail != null));
		}
		Db.logs().addLog(log);
	}

	private static void printInConsole(Log log) {
		String color = log.category.getColor();

		String category = color + "[" + log.category + "]";
		String email = color + "[Email] " + log.getUserEmail();
		String date = color + "[Date Time] " + DateAndTime.getFormattedDateAndTimeFromMs(log.createdAt);

		System.out.println(category);
		if (log.getUserEmail() != null)
			System.out.println(email);
		System.out.println(date);

		for (String message : log.getMessages()) {
			System.out.println(color + " - " + message);
		}
	}

	public static void warning(String userEmail, List<String> messages) {
		createLog(Category.WARNING, userEmail, messages);
	}

	public static void info(String userEmail, List<String> messages) {
		createLog(Category.INFO, userEmail, messages);
	}

	public static void info(String... messages) {
		info(null, Arrays.stream(messages).toList());
	}

	public static void newUser(String userEmail, List<String> messages) {
		createLog(Category.NEW_USER, userEmail, messages);
	}

	public static void exception(String userEmail, List<String> messages) {
		createLog(Category.EXCEPTION, userEmail, messages);
	}

	public static void exception(String... messages) {
		exception(null, Arrays.stream(messages).toList());
	}

	@Override
	public String toString() {
		return "Log{" +
				"createdAt=" + createdAt +
				", category=" + category +
				", messages=" + messages +
				", userEmail='" + userEmail + '\'' +
				'}';
	}

	public enum Category {
		INFO,
		NEW_USER,
		WARNING,
		EXCEPTION;

		public static Category fromString(String s) throws InvalidLogCategoryException {
			return switch(s) {
				case "INFO" -> INFO;
				case "NEW_USER" -> NEW_USER;
				case "WARNING" -> WARNING;
				case "EXCEPTION" -> EXCEPTION;
				default -> throw new InvalidLogCategoryException(s);
			};
		}

		public String getColor() {
			return switch(this) {
				case INFO -> Color.INFO;
				case NEW_USER -> Color.SUCCESS;
				case WARNING -> Color.WARNING;
				default -> Color.EXCEPTION;
			};
		}
	}

	private static class Color {
		public static final String INFO = "\u001B[34m";
		public static final String EXCEPTION = "\u001B[31m";
		public static final String WARNING = "\u001B[33m";
		public static final String SUCCESS = "\u001B[32m";
	}
}
