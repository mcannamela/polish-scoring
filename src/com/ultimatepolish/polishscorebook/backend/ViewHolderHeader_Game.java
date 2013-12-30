package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;
import java.util.List;

public class ViewHolderHeader_Game {
	private String name;
	private List<ViewHolder_Game> gameList = new ArrayList<ViewHolder_Game>();;
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ViewHolder_Game> getGameList() {
		return gameList;
	}
	public void setGameList(List<ViewHolder_Game> gameList) {
		this.gameList = gameList;
	}
}
