package com.lexivo.util;

import java.security.SecureRandom;

public abstract class Randomizer {
	public static String getEmailConfirmationCode() {
		SecureRandom random = new SecureRandom();
		int origin = 1_000_000;
		return String.valueOf(random.nextInt(origin, origin * 10));
	}
}
