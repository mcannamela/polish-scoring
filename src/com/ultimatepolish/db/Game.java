package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ultimatepolish.enums.ThrowResult;
import com.ultimatepolish.enums.ThrowType;

@DatabaseTable
public class Game {
	public static final String FIRST_PLAYER = "firstPlayer_id";
	public static final String SECOND_PLAYER = "secondPlayer_id";
	public static final String SESSION = "session_id";
	public static final String VENUE = "venue_id";
	public static final String DATE_PLAYED = "datePlayed";
	public static final String IS_COMPLETE = "isComplete";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(canBeNull = false, foreign = true)
	private Player firstPlayer;

	@DatabaseField(canBeNull = false, foreign = true)
	private Player secondPlayer;

	@DatabaseField(foreign = true)
	private Session session;

	@DatabaseField(foreign = true)
	private Venue venue;

	@DatabaseField(canBeNull = false)
	public int ruleSetId;

	@DatabaseField(canBeNull = false)
	public boolean firstPlayerOnTop;

	@DatabaseField(canBeNull = false)
	private Date datePlayed;

	@DatabaseField
	private int firstPlayerScore;

	@DatabaseField
	private int secondPlayerScore;

	@DatabaseField
	private boolean isComplete = false;

	@DatabaseField
	private boolean isTracked = true;

	public Game() {
		super();
	}

	public Game(Player firstPlayer, Player secondPlayer, Session session,
			Venue venue, int ruleSet, boolean isTracked, Date datePlayed) {
		super();
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.session = session;
		this.venue = venue;
		this.ruleSetId = ruleSet;
		this.isTracked = isTracked;
		this.datePlayed = datePlayed;

	}

	public Game(Player firstPlayer, Player secondPlayer, Session session,
			Venue venue, int ruleSet, boolean isTracked) {
		super();
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.session = session;
		this.venue = venue;
		this.ruleSetId = ruleSet;
		this.isTracked = isTracked;
		this.datePlayed = new Date();
	}

	public static Dao<Game, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Game, Long> d = null;
		try {
			d = helper.getGameDao();
		} catch (SQLException e) {
			throw new RuntimeException("Couldn't get game dao: ", e);
		}
		return d;
	}

	public static List<Game> getAll(Context context) throws SQLException {
		Dao<Game, Long> d = Game.getDao(context);
		List<Game> games = new ArrayList<Game>();
		for (Game g : d) {
			games.add(g);
		}
		return games;
	}

	public boolean isValidThrow(Throw t) {
		boolean isValid = true;
		int idx = t.throwIdx;
		switch (idx % 2) {
		// TODO: do players need to be refreshed now that foreign variable is
		// used?
		// first player is on offense
		case 0:
			isValid = isValid && (t.getOffensivePlayer() == firstPlayer);
			break;
		// second player is on defense
		case 1:
			isValid = isValid && (t.getOffensivePlayer() == secondPlayer);
			break;
		default:
			throw new RuntimeException("invalid index " + idx);
		}
		return isValid;
	}

	public ArrayList<Throw> getThrowList(Context context) throws SQLException {
		int tidx, maxThrowIndex;
		ArrayList<Throw> throwArray = new ArrayList<Throw>();

		HashMap<Integer, Throw> throwMap = new HashMap<Integer, Throw>();
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("game_id", getId());

		Dao<Throw, Long> d = Throw.getDao(context);
		List<Throw> dbThrows = d.queryForFieldValuesArgs(m);

		maxThrowIndex = 0;
		if (!dbThrows.isEmpty()) {
			Collections.sort(dbThrows);

			for (Throw t : dbThrows) {
				tidx = t.throwIdx;

				// purge any throws with negative index
				if (tidx < 0) {
					d.delete(t);
				}

				// populate the map
				throwMap.put(tidx, t);

				// keep track of the maximum index
				if (tidx > maxThrowIndex) {
					maxThrowIndex = tidx;
				}
			}

			// ensure throws in correct order and complete
			Throw t = null;
			for (int i = 0; i <= maxThrowIndex; i++) {
				t = throwMap.get(i);
				// infill with a caught strike if necessary
				if (t == null) {
					t = makeNewThrow(i);
					t.throwType = ThrowType.STRIKE;
					t.throwResult = ThrowResult.CATCH;
				}
				throwArray.add(t);
			}
		}

		return throwArray;
	}

	public Throw makeNewThrow(int throwNumber) {
		Player offensivePlayer, defensivePlayer;
		if (throwNumber % 2 == 0) {
			offensivePlayer = getFirstPlayer();
			defensivePlayer = getSecondPlayer();
		} else {
			offensivePlayer = getSecondPlayer();
			defensivePlayer = getFirstPlayer();
		}
		Date timestamp = new Date(System.currentTimeMillis());
		Throw t = new Throw(throwNumber, this, offensivePlayer,
				defensivePlayer, timestamp);

		return t;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public void setFirstPlayer(Player firstPlayer) {
		this.firstPlayer = firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public void setSecondPlayer(Player secondPlayer) {
		this.secondPlayer = secondPlayer;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public Date getDatePlayed() {
		return datePlayed;
	}

	public void setDatePlayed(Date datePlayed) {
		this.datePlayed = datePlayed;
	}

	public int getFirstPlayerScore() {
		return firstPlayerScore;
	}

	public void setFirstPlayerScore(int firstPlayerScore) {
		this.firstPlayerScore = firstPlayerScore;
		checkGameComplete();
	}

	public int getSecondPlayerScore() {
		return secondPlayerScore;
	}

	public void setSecondPlayerScore(int secondPlayerScore) {
		this.secondPlayerScore = secondPlayerScore;
		checkGameComplete();
	}

	public boolean getIsComplete() {
		return isComplete;
	}

	public void setIsComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	public boolean getIsTracked() {
		return isTracked;
	}

	public void checkGameComplete() {
		Integer s1 = getFirstPlayerScore();
		Integer s2 = getSecondPlayerScore();
		if (Math.abs(s1 - s2) >= 2 && (s1 >= 11 || s2 >= 11)) {
			setIsComplete(true);
		} else {
			setIsComplete(false);
		}
	}

	public Player getWinner() {
		// TODO: should raise an error if game is not complete
		Player winner = firstPlayer;
		if (getSecondPlayerScore() > getFirstPlayerScore()) {
			winner = secondPlayer;
		}
		return winner;
	}

	public Player getLoser() {
		Player loser = firstPlayer;
		if (getSecondPlayerScore() < getFirstPlayerScore()) {
			loser = secondPlayer;
		}
		return loser;
	}
}
