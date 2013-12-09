package com.ultimatepolish.polishscorebook;

public class NavDrawerItem {
	public String label;
	public int iconId;
	public int counter;
	public boolean isHeader;

	public NavDrawerItem() {
	}

	public NavDrawerItem(String label, int iconId, boolean isHeader) {
		this.label = label;
		this.iconId = iconId;
		this.counter = 0;
		this.isHeader = isHeader;
	}
}
