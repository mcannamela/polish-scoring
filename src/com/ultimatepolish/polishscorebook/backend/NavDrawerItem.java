package com.ultimatepolish.polishscorebook.backend;

public class NavDrawerItem {
	public String label;
	public int iconId;
	public int counter;
	public boolean isHeader;

	public NavDrawerItem() {
		this.label = "";
		this.iconId = 0;
		this.counter = 0;
		this.isHeader = true;
	}

	public NavDrawerItem(String label, int iconId) {
		this.label = label;
		this.iconId = iconId;
		this.counter = 0;
		this.isHeader = false;
	}
}
