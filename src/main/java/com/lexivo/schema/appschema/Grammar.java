package com.lexivo.schema.appschema;

import java.util.ArrayList;
import java.util.List;

public class Grammar {
	public final String id;
	public final String header;
	private final List<GrammarSubmenu> submenuList;

	public Grammar(String id, String header, List<GrammarSubmenu> submenuList) {
		this.id = id;
		this.header = header;
		this.submenuList = submenuList;
	}

	public List<GrammarSubmenu> getSubmenuList() {
		return new ArrayList<>(submenuList);
	}
}
