package com.ultimatepolish.polishscorebook.backend;

public class ViewHolder_Player {
	public String name;
    public String nickName;
    public String id;
    public int playerColor;
    
    public String getId(){
    	return id;
    }
    public String getName(){
    	return name;
    }
    public String getNickName(){
    	return nickName;
    }
    public Integer getColor(){
    	return playerColor;
    }
    
    public void setId(String playerId){
    	this.id = playerId;
    }
    public void setName(String playerName){
    	this.name = playerName;
    }
    public void setNickName(String playerNickName){
    	this.nickName = playerNickName;
    }
    public void setColor(Integer playerColor){
    	this.playerColor = playerColor;
    }
}
