package com.lexivo.schema.appschema;

public class Word {
	public final String id;
	public final String type;
	public final String level;
	public final String gender;
	public final int practiceCountdown;
	public final String ntv;
	public final String ntvDetails;
	public final String plural;
	public final String past1;
	public final String past2;
	public final String desc;
	public final String descDetails;

	public Word(String id, String type, String level, String gender, int practiceCountdown, String ntv, String ntvDetails, String plural, String past1, String past2, String desc, String descDetails) {
		this.id = id;
		this.type = type;
		this.level = level;
		this.gender = gender;
		this.practiceCountdown = practiceCountdown;
		this.ntv = ntv;
		this.ntvDetails = ntvDetails;
		this.plural = plural;
		this.past1 = past1;
		this.past2 = past2;
		this.desc = desc;
		this.descDetails = descDetails;
	}
}
