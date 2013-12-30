package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Session {
	public static final String IS_ACTIVE = "isActive";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(canBeNull = false)
	private String sessionName;

	@DatabaseField(canBeNull = false)
	public int sessionType;

	@DatabaseField
	private int ruleSetId;

	@DatabaseField(canBeNull = false)
	private Date startDate;

	@DatabaseField(canBeNull = true)
	private Date endDate;

	@DatabaseField
	private boolean isTeam = false;

	@DatabaseField
	private boolean isActive = true;

	@ForeignCollectionField
	ForeignCollection<Game> games;

	public Session() {
	}

	public Session(String sessionName, int sessionType, int ruleSetId,
			Date startDate, boolean isTeam) {
		super();
		this.sessionName = sessionName;
		this.sessionType = sessionType;
		this.ruleSetId = ruleSetId;
		this.startDate = startDate;
		this.isTeam = isTeam;

	}

	public static Dao<Session, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Session, Long> d = null;
		try {
			d = helper.getSessionDao();
		} catch (SQLException e) {
			throw new RuntimeException("Couldn't get session dao: ", e);
		}
		return d;
	}

	public static List<Session> getAll(Context context) throws SQLException {
		Dao<Session, Long> d = Session.getDao(context);
		List<Session> sessions = new ArrayList<Session>();
		for (Session s : d) {
			sessions.add(s);
		}
		return sessions;
	}

	public long getId() {
		return id;
	}

	// public void setId(long id) {
	// this.id = id;
	// }

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getSessionType() {
		return sessionType;
	}

	public int getRuleSetId() {
		return ruleSetId;
	}

	public void setSessionType(int sessionType) {
		this.sessionType = sessionType;
	}

	public boolean getIsTeam() {
		return isTeam;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public ForeignCollection<Game> getGames() {
		return games;
	}
}
