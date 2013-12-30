package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;
import java.util.List;

public class ViewHolderHeader_Venue {
	private String name;
	private List<ViewHolder_Venue> venueList = new ArrayList<ViewHolder_Venue>();
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ViewHolder_Venue> getVenueList() {
		return venueList;
	}
	public void setVenueList(List<ViewHolder_Venue> venueList) {
		this.venueList = venueList;
	}
}
