package com.ultimatepolish.polishscorebook.backend;

public class ViewHolder_Game {
	public String id;
    public String playerOne;
    public String playerTwo;
    public String score;
    
    public String getId(){
    	return id;
    }
    public String getPlayerOne(){
    	return playerOne;
    }
    public String getPlayerTwo(){
    	return playerTwo;
    }
    public String getScore(){
    	return score;
    }
    
    public void setId(String gameId){
    	this.id = gameId;
    }
    public void setPlayerOne(String gamePlayerOne){
    	this.playerOne = gamePlayerOne;
    }
    public void setPlayerTwo(String gamePlayerTwo){
    	this.playerTwo = gamePlayerTwo;
    }
    public void setScore(String gameScore){
    	this.score = gameScore;
    }
}
