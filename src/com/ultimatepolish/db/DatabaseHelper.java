package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ultimatepolish.polishscorebook.R;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "polish.db";
	private static final int DATABASE_VERSION = 11;

	private Dao<Player, Long> playerDao;
	private Dao<PlayerStats, Long> playerStatsDao;
	private Dao<Game, Long> gameDao;
	private Dao<Throw, Long> throwDao;
	private Dao<SessionMember, Long> sessionMemberDao;
	private Dao<Badge, Long> badgeDao;
	
//	private Dao<Team, Long> teamDao;
//	private Dao<TeamStats, Long> teamStatsDao;
//	private Dao<TeamGame, Long> teamGameDao;
//	private Dao<TeamThrow, Long> teamThrowDao;
//	private Dao<TeamSessionMember, Long> teamSessionMemberDao;
//	private Dao<TeamBadge, Long> teamBadgeDao;
	
	private Dao<Session, Long> sessionDao;
	private Dao<Venue, Long> venueDao;
	
	private List<Class> tableClasses = new ArrayList<Class>();
	
	private Context myContext;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
		tableClasses.add( Player.class);
		tableClasses.add( PlayerStats.class);
		tableClasses.add( Game.class);
		tableClasses.add( Throw.class);
		tableClasses.add( SessionMember.class);
		tableClasses.add( Badge.class);
		
//		tableClasses.add( Team.class);
//		tableClasses.add( TeamStats.class);
//		tableClasses.add( TeamGame.class);
//		tableClasses.add( TeamThrow.class);
//		tableClasses.add( TeamSessionMember.class);
//		tableClasses.add( TeamBadge.class);
		
		tableClasses.add( Session.class);
		tableClasses.add( Venue.class);
		
		myContext = context;
	}
	

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		Log.i("DatabaseHelper.onCreate()", "Attempting to create db");
		try {
			createAll(connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create database", e);
		}
	}

	@Override
	public void onUpgrade(final SQLiteDatabase sqliteDatabase, final ConnectionSource connectionSource, int oldVer, final int newVer) {
		Log.i("DatabaseHelper.onUpgrade()", "Attempting to upgrade from version " + oldVer + " to version " + newVer);
		
		switch (oldVer){
			case 9:
				increment_09(sqliteDatabase, connectionSource);
			case 10:
				increment_10(sqliteDatabase, connectionSource);
				break;
			default:
				try {
					dropAll(connectionSource);
					createAll(connectionSource);
				} catch (SQLException e) {
					Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to "
							+ newVer, e);
				}
		}
	}
	
	private void increment_09(SQLiteDatabase sqliteDatabase, 
			ConnectionSource connectionSource){
		try {
			Log.i("DatabaseHelper.increment_09", "Attempting to upgrade from version 09 to version 10");
			Dao<Game, Long> gDao = getGameDao();
			Dao<Player, Long> pDao = getPlayerDao();
			Dao<Session, Long> sDao = getSessionDao();
			Dao<Venue, Long> vDao = getVenueDao();
			Dao<Throw, Long> tDao = getThrowDao();
			
			DatabaseUpgrader.increment_09(connectionSource, gDao, pDao, sDao, vDao, tDao);
			
			createAll();
			
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + 9 + " to "
					+ 10, e);
		} 
	}
	
	private void increment_10(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource){
		try {
			Log.i("DatabaseHelper.increment_10", "Attempting to upgrade from version 10 to version 11");
			// throw table
			Dao<Game, Long> gDao = getGameDao();
			Dao<Throw, Long> tDao = getThrowDao();
			DatabaseUpgrader.increment_10(connectionSource, gDao, tDao);

		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + 10 + " to "
					+ 11, e);
		}
	}
	
	public void createAll(){
		try{
			createAll(getConnectionSource());
		}
		catch (SQLException e){
			Log.e(DatabaseHelper.class.toString(), e.getMessage());
			throw new RuntimeException("could not create tables",e);
		}
	}
	
	public void dropAll(){
		try{
			dropAll(getConnectionSource());
		}
		catch (SQLException e){
			Log.e(DatabaseHelper.class.toString(), e.getMessage());
			throw new RuntimeException("could not drop tables",e);
		}
	}
	
	protected void createAll(ConnectionSource connectionSource) throws SQLException{
		for(Class c:tableClasses){
			TableUtils.createTableIfNotExists(connectionSource, c);
		}
	}
	
	protected void dropAll(ConnectionSource connectionSource) throws SQLException{
		for(Class c:tableClasses){
			TableUtils.dropTable(connectionSource, c, true);
		}
	}

	public Dao<Player, Long> getPlayerDao() throws SQLException {
		if (playerDao == null) {
			playerDao = getDao(Player.class);
		}
		return playerDao;
	}
	public Dao<PlayerStats, Long> getPlayerStatsDao() throws SQLException {
		if (playerStatsDao == null) {
			playerStatsDao = getDao(PlayerStats.class);
		}
		return playerStatsDao;
	}
//	public Dao<Team, Long> getTeamDao() throws SQLException {
//		if (teamDao == null) {
//			teamDao = getDao(Team.class);
//		}
//		return teamDao;
//	}
//	public Dao<TeamStats, Long> getTeamStatsDao() throws SQLException {
//		if (teamStatsDao == null) {
//			teamStatsDao = getDao(TeamStats.class);
//		}
//		return teamStatsDao;
//	}
	public Dao<Badge, Long> getBadgeDao() throws SQLException {
		if (badgeDao == null) {
			badgeDao = getDao(Badge.class);
		}
		return badgeDao;
	}
	public Dao<Game, Long> getGameDao() throws SQLException {
		if (gameDao == null) {
			gameDao = getDao(Game.class);
		}
		return gameDao;
	}
	public Dao<Session, Long> getSessionDao() throws SQLException {
		if (sessionDao == null) {
			sessionDao = getDao(Session.class);
		}
		return sessionDao;
	}
	public Dao<SessionMember, Long> getSessionMemberDao() throws SQLException {
		if (sessionMemberDao == null) {
			sessionMemberDao = getDao(SessionMember.class);
		}
		return sessionMemberDao;
	}
	public Dao<Throw, Long> getThrowDao() throws SQLException {
		if (throwDao == null) {
			throwDao = getDao(Throw.class);
		}
		return throwDao;
	}
	public Dao<Venue, Long> getVenueDao() throws SQLException {
		if (venueDao == null) {
			venueDao = getDao(Venue.class);
		}
		return venueDao;
	}
}
