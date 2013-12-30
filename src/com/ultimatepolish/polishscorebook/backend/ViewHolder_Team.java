package com.ultimatepolish.polishscorebook.backend;

public class ViewHolder_Team {
	public String id;
	public String teamName;
	public String playerNames;
    
    public String getId(){
    	return id;
    }
    public String getTeamName(){
    	return teamName;
    }
    public String getPlayerNames(){
    	return playerNames;
    }
    
    public void setId(String teamId){
    	this.id = teamId;
    }
    public void setTeamName(String teamName){
    	this.teamName = teamName;
    }
    public void setPlayerNames(String playerNames){
    	this.playerNames = playerNames;
    }

}
