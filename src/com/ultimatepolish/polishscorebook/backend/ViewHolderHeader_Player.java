package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;

public class ViewHolderHeader_Player {
	private String name;
	private ArrayList<ViewHolder_Player> playerList = new ArrayList<ViewHolder_Player>();
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<ViewHolder_Player> getPlayerList() {
		return playerList;
	}
	public void setPlayerList(ArrayList<ViewHolder_Player> playerList) {
		this.playerList = playerList;
	}
}
