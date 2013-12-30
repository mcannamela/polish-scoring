package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;

public class ViewHolderHeader_Session {
	private String name;
	private ArrayList<ViewHolder_Session> sessionList = new ArrayList<ViewHolder_Session>();
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<ViewHolder_Session> getSessionList() {
		return sessionList;
	}
	public void setSessionList(ArrayList<ViewHolder_Session> sessionList) {
		this.sessionList = sessionList;
	}
}
