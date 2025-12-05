package com.lexivo.util;

import java.time.Instant;
import java.util.List;
import java.util.Random;

public abstract class Randomizer {
	public static List<Long> getEmailConfirmationNumberAndDateList() {
		Random random = new Random();
		int origin = 1_000_000;
		return List.of((long)random.nextInt(origin, origin * 10), Instant.now().toEpochMilli());
	}
}
