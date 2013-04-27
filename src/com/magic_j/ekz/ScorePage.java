package com.magic_j.ekz;

public class ScorePage {

	public String ekzName;
	public int page;
	public String type;
	
	public ScorePage(String ekzName, int page, String type) {
		this.ekzName = ekzName;
		this.page = page;
		this.type = type;
	}
	
	public String getBoardId() {		
		return type + ":" + page + "_" + ekzName;
	}

}
