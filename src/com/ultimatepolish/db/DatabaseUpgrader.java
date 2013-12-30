package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ultimatepolish.enums.DeadType;
import com.ultimatepolish.enums.RuleType;
import com.ultimatepolish.enums.ThrowResult;
import com.ultimatepolish.enums.ThrowType;
import com.ultimatepolish.polishscorebook.backend.ActiveGame;
import com.ultimatepolish.rulesets.RuleSet;

public class DatabaseUpgrader {
	public static void increment_09(ConnectionSource connectionSource,
			Dao<Game, Long> gDao, Dao<Player, Long> pDao,
			Dao<Session, Long> sDao, Dao<Venue, Long> vDao,
			Dao<Throw, Long> tDao) throws SQLException {

		String addGameColumn = "ALTER TABLE game ADD COLUMN ";
		gDao.executeRaw(addGameColumn + "isComplete BOOLEAN DEFAULT 1;");
		gDao.executeRaw(addGameColumn + "isTracked BOOLEAN DEFAULT 1;");
		gDao.executeRaw(addGameColumn + "ruleSetId INTEGER DEFAULT 0;");

		// clean out games with non-unique players
		GenericRawResults<String[]> rawResults = gDao
				.queryRaw("SELECT * FROM game WHERE firstPlayerId == secondPlayerId");
		List<String[]> results = rawResults.getResults();
		for (String[] gRes : results) {
			String gId = gRes[0];
			tDao.executeRaw("DELETE FROM throw WHERE gameId == " + gId + ";");
		}
		gDao.executeRaw("DELETE FROM game WHERE firstPlayerId == secondPlayerId;");

		// continue migrating game table
		pDao.executeRaw("ALTER TABLE game RENAME TO temp;");
		TableUtils.createTable(connectionSource, Game.class);
		pDao.executeRaw("INSERT INTO game(id, firstPlayer_id, secondPlayer_id, session_id, venue_id, ruleSetId, firstPlayerOnTop, datePlayed, firstPlayerScore, secondPlayerScore, isComplete, isTracked) "
				+ "SELECT id, firstPlayerId, secondPlayerId, sessionId, venueId, ruleSetId, firstPlayerOnTop, datePlayed, firstPlayerScore, secondPlayerScore, isComplete, isTracked FROM temp;");
		pDao.executeRaw("DROP TABLE temp;");

		// mark completed games as appropriate
		for (Game g : gDao) {
			g.checkGameComplete();
			gDao.update(g);
		}

		// player table
		pDao.executeRaw("ALTER TABLE player ADD COLUMN isActive BOOLEAN DEFAULT 1;");
		pDao.executeRaw("ALTER TABLE player ADD COLUMN prefersRightSide BOOLEAN DEFAULT 0;");
		pDao.executeRaw("ALTER TABLE player ADD COLUMN prefersLeftSide BOOLEAN DEFAULT 0;");
		pDao.executeRaw("ALTER TABLE player ADD COLUMN color INTEGER DEFAULT "
				+ Color.BLACK + ";");
		pDao.executeRaw("ALTER TABLE player RENAME TO temp;");
		TableUtils.createTable(connectionSource, Player.class);
		pDao.executeRaw("INSERT INTO player(id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, prefersRightSide, prefersLeftSide, height_cm, weight_kg, color, isActive) "
				+ "SELECT id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, prefersRightSide, prefersLeftSide, height_cm, weight_kg, color, isActive FROM temp;");
		pDao.executeRaw("DROP TABLE temp;");

		// session table
		String addSessionColumn = "ALTER TABLE session ADD COLUMN ";
		sDao.executeRaw(addSessionColumn + "sessionType INT DEFAULT 1;");
		sDao.executeRaw("UPDATE session SET sessionType = 0 WHERE sessionName = 'side_books';");
		sDao.executeRaw(addSessionColumn + "isTeam BOOLEAN DEFAULT 0;");
		sDao.executeRaw(addSessionColumn + "isActive BOOLEAN DEFAULT 1;");
		sDao.executeRaw(addSessionColumn + "ruleSetId INTEGER DEFAULT -1;");
		sDao.executeRaw("UPDATE session SET ruleSetId = 1 WHERE sessionName = 'league';");
		sDao.executeRaw("ALTER TABLE session RENAME TO temp;");
		TableUtils.createTable(connectionSource, Session.class);
		sDao.executeRaw("INSERT INTO session(id, sessionName, sessionType, ruleSetId, startDate, endDate, isTeam, isActive) "
				+ "SELECT id, sessionName, sessionType, ruleSetId, startDate, endDate, isTeam, isActive FROM temp;");
		sDao.executeRaw("DROP TABLE temp;");

		// venue table
		String addVenueColumn = "ALTER TABLE venue ADD COLUMN ";
		vDao.executeRaw(addVenueColumn + "longitude LONG;");
		vDao.executeRaw(addVenueColumn + "latitude LONG;");
		vDao.executeRaw(addVenueColumn + "zipCode LONG;");
		vDao.executeRaw(addVenueColumn + "isActive BOOLEAN DEFAULT 1;");
		vDao.executeRaw("ALTER TABLE venue RENAME TO temp;");
		TableUtils.createTable(connectionSource, Venue.class);
		vDao.executeRaw("INSERT INTO venue(id, venueName, scoreKeptFromTop, longitude, latitude, zipCode, isActive) "
				+ "SELECT id, name, scoreKeptFromTop, longitude, latitude, zipCode, isActive FROM temp;");
		vDao.executeRaw("DROP TABLE temp;");
	}

	public static void increment_10(ConnectionSource connectionSource,
			Dao<Game, Long> gDao, Dao<Throw, Long> tDao) throws SQLException {

		// throw table
		// add all the new column types. after populating the new columns, the
		// table gets rebuilt
		// these are easy. tipped and linefault were not tracked at all
		// previously.
		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.NOT_THROWN
				+ " WHERE throwType=8;"); // NOT_THROWN moved from 8 to 11

		tDao.executeRaw(addBooleanDefaultZeroColumn("throw", "isTipped"));
		tDao.executeRaw(addBooleanDefaultZeroColumn("throw", "isLineFault"));

		// populating defensivePlayerId column requires looping through all the
		// games
		tDao.executeRaw("ALTER TABLE throw ADD COLUMN defensivePlayerId INTEGER;");
		for (Game g : gDao) {
			tDao.executeRaw("UPDATE throw SET defensivePlayerId="
					+ g.getFirstPlayer().getId() + " WHERE gameId=" + g.getId()
					+ " AND playerId= " + g.getSecondPlayer().getId() + ";");
			tDao.executeRaw("UPDATE throw SET defensivePlayerId="
					+ g.getSecondPlayer().getId() + " WHERE gameId="
					+ g.getId() + " AND playerId= "
					+ g.getFirstPlayer().getId() + ";");
		}

		// since games are set to manual fire ruleset, fire is not calculated.
		tDao.executeRaw(addColumn("throw", "offenseFireCount", "INTEGER", "0"));
		tDao.executeRaw(replaceNulls("throw", "offenseFireCount", "0"));
		tDao.executeRaw("UPDATE throw SET offenseFireCount=3 WHERE isOnFire=1;");

		tDao.executeRaw(addColumn("throw", "defenseFireCount", "INTEGER", "0"));
		tDao.executeRaw(replaceNulls("throw", "defenseFireCount", "0"));
		tDao.executeRaw("UPDATE throw SET defenseFireCount=3 WHERE isFiredOn=1;");

		// other columns
		tDao.executeRaw(addColumn("throw", "deadType", "INTEGER", "0"));
		tDao.executeRaw(replaceNulls("throw", "deadType", "0"));

		// ////////ADD NEW COLUMNS ///////////////////
		// isGoaltend (db10) == > isGoaltend (db11)
		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.POLE
				+ " WHERE isGoaltend=1 AND goaltendScore=2;");
		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.BOTTLE
				+ " WHERE isGoaltend=1 AND goaltendScore=3;");

		// isDrinkDropped (db10) ==> isDefensiveDrinkDropped (db11)
		String[] columnNames = { "isOffensiveDrinkDropped",
				"isOffensivePoleKnocked", "isOffensiveBottleKnocked",
				"isOffensiveBreakError", "isGrabbed", "isDefensivePoleKnocked",
				"isDefensiveBottleKnocked", "isDefensiveBreakError" };

		for (int i = 0; i < columnNames.length; i++) {
			tDao.executeRaw(addBooleanDefaultZeroColumn("throw", columnNames[i]));
			tDao.executeRaw(replaceNulls("throw", columnNames[i], "0"));
		}

		// //////////////////////////////////////////////////////////////////
		// ////////////// MIGRATE FIRE//////////////////////////
		// //////////////////////////////////////////////////////////////////
		tDao.executeRaw("UPDATE throw SET isTipped=1 WHERE isOnFire=1 AND throwResult="
				+ ThrowResult.CATCH
				+ " AND "
				+ "(throwType="
				+ ThrowType.POLE
				+ " OR "
				+ "throwType="
				+ ThrowType.CUP
				+ " OR "
				+ "throwType="
				+ ThrowType.BOTTLE + ")" + ";");
		tDao.executeRaw("UPDATE throw SET throwResult=" + ThrowResult.NA
				+ " WHERE isOnFire=1 OR isFiredOn=1;");
		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.FIRED_ON
				+ " WHERE isFiredOn=1;");

		// //////////////////////////////////////////////////////////////////
		// ////////////// MIGRATE OFFENSIVE ERRORS //////////////////////////
		// //////////////////////////////////////////////////////////////////
		// drink drop and break errors werent tracked before so stay false.
		tDao.executeRaw("UPDATE throw SET throwResult=" + ThrowResult.BROKEN
				+ " WHERE isBroken=1;");
		tDao.executeRaw("UPDATE throw SET isOffensivePoleKnocked=1 WHERE ownGoalScore=2 AND isOwnGoal=1;");
		tDao.executeRaw("UPDATE throw SET isOffensiveBottleKnocked=1 WHERE ownGoalScore=3 AND isOwnGoal=1;");

		// //////////////////////////////////////////////////////////////////
		// ////////////// MIGRATE SHORT/TRAPPED //////////////////////////
		// //////////////////////////////////////////////////////////////////
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.LOW
				+ " WHERE isShort=1;"); // Ball_low and default deadtype
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.HIGH
				+ " WHERE isShort=1 AND throwType=" + ThrowType.BALL_HIGH + ";");
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.RIGHT
				+ " WHERE isShort=1 AND throwType=" + ThrowType.BALL_RIGHT
				+ ";");
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.LEFT
				+ " WHERE isShort=1 AND throwType=" + ThrowType.BALL_LEFT + ";");

		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.SHORT
				+ " WHERE isShort=1;");
		tDao.executeRaw("UPDATE throw SET throwResult=" + ThrowResult.NA
				+ " WHERE isShort=1;");

		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.HIGH
				+ " WHERE isTrap=1;"); // Ball_high and default deadtype
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.LOW
				+ " WHERE isTrap=1 AND throwType=" + ThrowType.BALL_LOW + ";");
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.RIGHT
				+ " WHERE isTrap=1 AND throwType=" + ThrowType.BALL_RIGHT + ";");
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.LEFT
				+ " WHERE isTrap=1 AND throwType=" + ThrowType.BALL_LEFT + ";");

		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.TRAP
				+ " WHERE isTrap=1;");
		tDao.executeRaw("UPDATE throw SET throwResult=" + ThrowResult.NA
				+ " WHERE isTrap=1;");

		// //////////////////////////////////////////////////////////////////
		// ////////////// MIGRATE DEFENSIVE ERRORS //////////////////////////
		// //////////////////////////////////////////////////////////////////

		// ///////// FIX FALSE POSITIVES ////////////////
		tDao.executeRaw("UPDATE throw SET errorScore=0 WHERE isError=0;");

		// //////// FIX E1's ///////////////////
		// errorScore=0 (PCB knocked but bottle caught) no longer tracked.
		tDao.executeRaw("UPDATE throw SET isGrabbed=1 WHERE errorScore=1 AND isError=1;");

		// //////// FIX E2's and E3s///////////////////
		// for errorScore=2/3, assume defense knock pole/bottle if throw was a
		// strike.
		// assume dead pole/bottle hit that wasnt caught if throw was HRLL
		tDao.executeRaw("UPDATE throw SET isDefensivePoleKnocked=1 "
				+ "WHERE errorScore=2 and throwType=" + ThrowType.STRIKE
				+ " AND isError=1;");
		tDao.executeRaw("UPDATE throw SET isDefensiveBottleKnocked=1 "
				+ "WHERE errorScore=3 and throwType=" + ThrowType.STRIKE
				+ " AND isError=1;");

		// HRLL with error score greater than 1 maps to dead HRLL
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.HIGH
				+ " WHERE isError=1 AND errorScore>1 and throwType="
				+ ThrowType.BALL_HIGH + ";"); // deadType to high
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.RIGHT
				+ " WHERE isError=1 AND errorScore>1 and throwType="
				+ ThrowType.BALL_RIGHT + ";"); // deadType to right
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.LOW
				+ " WHERE isError=1 AND errorScore>1 and throwType="
				+ ThrowType.BALL_LOW + ";"); // deadType to low
		tDao.executeRaw("UPDATE throw SET deadType=" + DeadType.LEFT
				+ " WHERE isError=1 AND errorScore>1 and throwType="
				+ ThrowType.BALL_LEFT + ";"); // deadType to left

		// throwType to pole
		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.POLE
				+ " WHERE isError=1 AND errorScore=2 AND throwType!="
				+ ThrowType.STRIKE + ";");
		// throwType to bottle
		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.BOTTLE
				+ " WHERE isError=1 AND errorScore=3 AND throwType!="
				+ ThrowType.STRIKE + ";");
		// throwResult to dropped
		tDao.executeRaw("UPDATE throw SET throwResult=" + ThrowResult.DROP
				+ " WHERE isError=1 AND errorScore>1 AND throwType!="
				+ ThrowType.STRIKE + ";");

		// Handle two special cases...
		tDao.executeRaw("UPDATE throw SET throwResult=" + ThrowResult.CATCH
				+ " WHERE throwType=" + ThrowType.BALL_HIGH
				+ " AND throwResult=" + ThrowResult.STALWART + ";");

		tDao.executeRaw("UPDATE throw SET throwType=" + ThrowType.BOTTLE
				+ " WHERE throwResult=" + ThrowResult.BROKEN
				+ " AND throwType=" + ThrowType.STRIKE + ";");

		// rebuild the table and copy data over
		tDao.executeRaw("ALTER TABLE throw RENAME TO temp;");
		TableUtils.createTable(connectionSource, Throw.class);
		tDao.executeRaw("INSERT INTO throw(id, throwIdx, game_id, offensivePlayer_id, defensivePlayer_id, timestamp, "
				+ "throwType, throwResult, deadType, isTipped, isGoaltend, isGrabbed, isDrinkHit, isLineFault, "
				+ "isOffensiveDrinkDropped, isOffensivePoleKnocked, isOffensiveBottleKnocked, isOffensiveBreakError, "
				+ "isDefensiveDrinkDropped, isDefensivePoleKnocked, isDefensiveBottleKnocked, isDefensiveBreakError, "
				+ "offenseFireCount, defenseFireCount, initialOffensivePlayerScore, initialDefensivePlayerScore) "
				+ "SELECT id, throwNumber, gameId, playerId, defensivePlayerId, timestamp, "
				+ "throwType, throwResult, deadType, isTipped, isGoaltend, isGrabbed, isDrinkHit, isLineFault, "
				+ "isOffensiveDrinkDropped, isOffensivePoleKnocked, isOffensiveBottleKnocked, isOffensiveBreakError, "
				+ "isDrinkDropped, isDefensivePoleKnocked, isDefensiveBottleKnocked, isDefensiveBreakError, "
				+ "offenseFireCount, defenseFireCount, initialOffensivePlayerScore, initialDefensivePlayerScore FROM temp;");
		tDao.executeRaw("DROP TABLE temp;");
	}

	public static String replaceNulls(String tableName, String columnName,
			String value) {
		return "UPDATE " + tableName + " SET " + columnName + "=" + value
				+ " where " + columnName + " is NULL;";

	}

	public static String addColumn(String tableName, String columnName,
			String type, String defaultValue) {
		return "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " "
				+ type + " DEFAULT " + defaultValue + ";";
	}

	public static String addBooleanDefaultZeroColumn(String tableName,
			String columnName) {
		return addColumn(tableName, columnName, "BOOLEAN", "0");
	}

	public static List<Long> updateScores(Dao<Game, Long> gDao, Context context) {
		ActiveGame ag = null;
		String msg;
		int[] oldScores = new int[2];
		int[] newScores = new int[2];
		List<Long> badGames = new ArrayList<Long>();
		for (Game g : gDao) {
			// Log.i("DatabaseUpgrader.updateScores()","processing game "+g.getId());
			oldScores[0] = g.getFirstPlayerScore();
			oldScores[1] = g.getSecondPlayerScore();

			ag = new ActiveGame(g.getId(), context, g.ruleSetId);
			// saveAllThrows is extremely slow. any way to speed up?
			ag.saveAllThrows(); // this also calls updateThrowsFrom(0)
			ag.saveGame();
			newScores[0] = ag.getGame().getFirstPlayerScore();
			newScores[1] = ag.getGame().getSecondPlayerScore();

			if (!(are_scores_equal(oldScores, newScores))) {
				msg = String.format("bad game %d: (%d,%d)->(%d,%d)", g.getId(),
						oldScores[0], oldScores[1], newScores[0], newScores[1]);
				Log.w("DatabaseUpgrader.updateScores()", msg);
				badGames.add(g.getId());
			}
		}
		return badGames;
	}

	public static List<Long> checkThrows(Dao<Throw, Long> tDao, Context context)
			throws SQLException {
		String msg;
		List<Long> badThrows = new ArrayList<Long>();
		// TODO: make this dynamic once implemented in db
		RuleSet rs = RuleType.RS00;
		for (Throw t : tDao) {
			if (!rs.isValid(t)) {
				msg = "bad throw: " + t.getId() + "- " + t.invalidMessage;
				Log.w("DatabaseUpgrader.checkThrows()", msg);
				badThrows.add(t.getId());
			}
		}
		return badThrows;
	}

	public static boolean are_scores_equal(int[] oldScores, int[] newScores) {
		return oldScores[0] == newScores[0] && oldScores[1] == newScores[1];
	}
}
