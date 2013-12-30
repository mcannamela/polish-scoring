package com.ultimatepolish.polishscorebook.backend;

public class ViewHolder_Session {
	public String id;
    public String name;
    public String type;
    public String team;
    
    public String getId(){
    	return id;
    }
    public String getName(){
    	return name;
    }
    public String getType(){
    	return type;
    }
    public String getTeam(){
    	return team;
    }
    
    public void setId(String sessionId){
    	this.id = sessionId;
    }
    public void setName(String sessionName){
    	this.name = sessionName;
    }
    public void setType(String sessionType){
    	this.type = sessionType;
    }
    public void setTeam(String sessionTeam){
    	this.team = sessionTeam;
    }
    
}
