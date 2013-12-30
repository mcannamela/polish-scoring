package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;
import java.util.List;

public class ViewHolderHeader_Team {
	private String name;
	private List<ViewHolder_Team> teamList = new ArrayList<ViewHolder_Team>();
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ViewHolder_Team> getTeamList() {
		return teamList;
	}
	public void setTeamList(List<ViewHolder_Team> teamList) {
		this.teamList = teamList;
	}
}
