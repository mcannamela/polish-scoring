package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Venue {
	public static final String VENUE_NAME = "venueName";
	public static final String IS_ACTIVE = "isActive";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(canBeNull = false, unique = true, columnName = VENUE_NAME)
	private String venueName;

	@DatabaseField(canBeNull = true)
	public boolean scoreKeptFromTop = true;

	@DatabaseField
	private long longitude;

	@DatabaseField
	private long latitude;

	@DatabaseField
	private long zipCode;

	@DatabaseField
	private boolean isActive = true;

	Venue() {
	}

	public Venue(String venueName, boolean scoreKeptFromTop) {
		super();
		this.venueName = venueName;
		this.scoreKeptFromTop = scoreKeptFromTop;
	}

	public static Dao<Venue, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Venue, Long> d = null;
		try {
			d = helper.getVenueDao();
		} catch (SQLException e) {
			throw new RuntimeException("Couldn't get venue dao: ", e);
		}
		return d;
	}

	public static List<Venue> getAll(Context context) throws SQLException {
		Dao<Venue, Long> d = Venue.getDao(context);
		List<Venue> venues = new ArrayList<Venue>();
		for (Venue v : d) {
			venues.add(v);
		}
		return venues;
	}

	public Venue(String venueName) {
		super();
		this.venueName = venueName;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return venueName;
	}

	public void setName(String venueName) {
		this.venueName = venueName;
	}

	public void setScoreFromTop(Boolean sfTop) {
		this.scoreKeptFromTop = sfTop;
	}

	public boolean exists(Context context) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
}
