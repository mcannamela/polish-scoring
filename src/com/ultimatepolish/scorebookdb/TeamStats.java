package com.ultimatepolish.scorebookdb;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class TeamStats{
	public static final String TEAM = "team_id";
	
	@DatabaseField(unique=true, foreign=true)
	private Team team;
	
	@DatabaseField
	public int nWins;
	
	@DatabaseField
	private int nLosses;
	
	TeamStats(){}

	public TeamStats(long teamId) {
		super();
	}
	
//	public static Dao<TeamStats, Long> getDao(Context context) throws SQLException{
//		DatabaseHelper helper = new DatabaseHelper(context);
//		Dao<TeamStats, Long> d = helper.getTeamStatsDao();
//		return d;
//	}

//	public static List<TeamStats> getAll(Context context) throws SQLException{
//		Dao<TeamStats, Long> d = TeamStats.getDao(context);
//		List<TeamStats> teamsStats = new ArrayList<TeamStats>();
//		for(TeamStats t:d){
//			teamsStats.add(t);
//		}
//		return teamsStats;
//	}

	public Team getTeam() {
		return team;
	}

	public int getnGames() {
		return nWins + nLosses;
	}

	public int getnWins() {
		return nWins;
	}

	public void setnWins(int nWins) {
		this.nWins = nWins;
	}

	public int getnLosses() {
		return nLosses;
	}

	public void setnLosses(int nLosses) {
		this.nLosses = nLosses;
	}
	
	
}
