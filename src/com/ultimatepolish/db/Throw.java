package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.ultimatepolish.enums.DeadType;
import com.ultimatepolish.enums.ThrowType;

public class Throw implements Comparable<Throw> {
	public static final String THROW_INDEX = "throwIdx";
	public static final String GAME_ID = "game_id";
	public static final String OFFENSIVE_PLAYER = "offensivePlayer_id";
	public static final String DEFENSIVE_PLAYER = "defensivePlayer_id";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(canBeNull = false, uniqueCombo = true)
	public int throwIdx;

	@DatabaseField(canBeNull = false, uniqueCombo = true, foreign = true)
	private Game game;

	@DatabaseField(canBeNull = false, foreign = true)
	private Player offensivePlayer;

	@DatabaseField(canBeNull = false, foreign = true)
	private Player defensivePlayer;

	@DatabaseField(canBeNull = false)
	private Date timestamp;

	@DatabaseField(canBeNull = false)
	public int throwType;

	@DatabaseField(canBeNull = false)
	public int throwResult;

	@DatabaseField
	public int deadType = DeadType.ALIVE;

	@DatabaseField
	public boolean isTipped = false;

	@DatabaseField
	public boolean isGoaltend = false;

	@DatabaseField
	public boolean isGrabbed = false;

	@DatabaseField
	public boolean isDrinkHit = false;

	@DatabaseField
	public boolean isLineFault = false;

	@DatabaseField
	public boolean isOffensiveDrinkDropped = false;

	@DatabaseField
	public boolean isOffensivePoleKnocked = false;

	@DatabaseField
	public boolean isOffensiveBottleKnocked = false;

	@DatabaseField
	public boolean isOffensiveBreakError = false;

	@DatabaseField
	public boolean isDefensiveDrinkDropped = false;

	@DatabaseField
	public boolean isDefensivePoleKnocked = false;

	@DatabaseField
	public boolean isDefensiveBottleKnocked = false;

	@DatabaseField
	public boolean isDefensiveBreakError = false;

	@DatabaseField
	public int offenseFireCount = 0;

	@DatabaseField
	public int defenseFireCount = 0;

	@DatabaseField
	public int initialOffensivePlayerScore = 0;

	@DatabaseField
	public int initialDefensivePlayerScore = 0;

	public String invalidMessage = "";

	Throw() {
	}

	public Throw(int throwIdx, Game game, Player offensivePlayer,
			Player defensivePlayer, Date timestamp, int throwType,
			int throwResult) {
		super();
		this.throwIdx = throwIdx;
		this.game = game;
		this.offensivePlayer = offensivePlayer;
		this.defensivePlayer = defensivePlayer;
		this.timestamp = timestamp;
		this.throwType = throwType;
		this.throwResult = throwResult;
	}

	public Throw(int throwIdx, Game game, Player offensivePlayer,
			Player defensivePlayer, Date timestamp) {
		super();
		this.throwIdx = throwIdx;
		this.game = game;
		this.offensivePlayer = offensivePlayer;
		this.defensivePlayer = defensivePlayer;
		this.timestamp = timestamp;
		this.throwType = ThrowType.NOT_THROWN;
	}

	public static Dao<Throw, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Throw, Long> d = null;
		try {
			d = helper.getThrowDao();
		} catch (SQLException e) {
			throw new RuntimeException("Couldn't get dao: ", e);
		}
		return d;
	}

	public HashMap<String, Object> getQueryMap() {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put(Throw.THROW_INDEX, throwIdx);
		m.put(Throw.GAME_ID, getGame());
		return m;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Game getGame() {
		return game;
	}

	public Player getOffensivePlayer() {
		return offensivePlayer;
	}

	public Player getDefensivePlayer() {
		return defensivePlayer;
	}

	public boolean[] getOwnGoals() {
		boolean[] ownGoals = { isLineFault, isOffensiveDrinkDropped,
				isOffensivePoleKnocked, isOffensiveBottleKnocked,
				isOffensiveBreakError };
		return ownGoals;
	}

	public void setOwnGoals(boolean[] ownGoals) {
		isLineFault = ownGoals[0];
		isOffensiveDrinkDropped = ownGoals[1];
		isOffensivePoleKnocked = ownGoals[2];
		isOffensiveBottleKnocked = ownGoals[3];
		isOffensiveBreakError = ownGoals[4];
	}

	public boolean[] getDefErrors() {
		boolean[] defErrors = { isGoaltend, isGrabbed, isDrinkHit,
				isDefensiveDrinkDropped, isDefensivePoleKnocked,
				isDefensiveBottleKnocked, isDefensiveBreakError };
		return defErrors;
	}

	public void setDefErrors(boolean[] defErrors) {
		isGoaltend = defErrors[0];
		isGrabbed = defErrors[1];
		isDrinkHit = defErrors[2];
		isDefensiveDrinkDropped = defErrors[3];
		isDefensivePoleKnocked = defErrors[4];
		isDefensiveBottleKnocked = defErrors[5];
		isDefensiveBreakError = defErrors[6];
	}

	public int compareTo(Throw another) {
		if (throwIdx < another.throwIdx) {
			return -1;
		} else if (throwIdx == another.throwIdx) {
			return 0;
		} else {
			return 1;
		}
	}

	public static boolean isP1Throw(int throwIdx) {
		return throwIdx % 2 == 0;
	}

	public static boolean isP1Throw(Throw t) {
		return isP1Throw(t.throwIdx);
	}

	public boolean isP1Throw() {
		return isP1Throw(throwIdx);
	}
	
	public boolean isBall(){
		return ThrowType.isBall(throwType);
	}
	
	public boolean isStackHit(){
		return ThrowType.isStackHit(throwType);
	}
	
	public boolean isStrike(){
		return throwType==ThrowType.STRIKE;
	}

	private void logd(String method, String msg) {
		Log.d("Throw" + "." + method, msg);
	}
}