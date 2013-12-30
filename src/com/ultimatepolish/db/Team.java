package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Team {
	public static final String TEAM_NAME = "teamName";
	public static final String FIRST_PLAYER = "firstPlayer_id";
	public static final String SECOND_PLAYER = "secondPlayer_id";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(canBeNull = false)
	private String teamName;

	@DatabaseField(canBeNull = false, uniqueCombo = true, foreign = true)
	private Player firstPlayer;

	@DatabaseField(canBeNull = false, uniqueCombo = true, foreign = true)
	private Player secondPlayer;

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	byte[] imageBytes;

	@DatabaseField
	private boolean isActive = true;

	Team() {
	}

	public Team(String teamName, Player firstPlayer, Player secondPlayer) {
		super();
		this.teamName = teamName;
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
	}

	public Team(String teamName, Player[] players) {
		super();
		this.teamName = teamName;
		this.firstPlayer = players[0];
		this.secondPlayer = players[1];
	}

	// public static Dao<Team, Long> getDao(Context context) throws
	// SQLException{
	// DatabaseHelper helper = new DatabaseHelper(context);
	// Dao<Team, Long> d = helper.getTeamDao();
	// return d;
	// }

	public static boolean exists(String teamName, Context context)
			throws SQLException {
		if (teamName == null) {
			return false;
		}
		List<Team> teamList = null;
		// HashMap<String,Object> m = buildNameMap(teamName);
		//
		// teamList = getDao(context).queryForFieldValuesArgs(m);
		// if (teamList.isEmpty()){
		return false;
		// }
		// else{
		// return true;
		// }
	}

	public boolean exists(Context context) throws SQLException {
		return exists(teamName, context);
	}

	// public static List<Team> getAll(Context context) throws SQLException{
	// Dao<Team, Long> d = Team.getDao(context);
	// List<Team> teams = new ArrayList<Team>();
	// for(Team t:d){
	// teams.add(t);
	// }
	// return teams;
	// }

	public long getId() {
		return id;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

}
