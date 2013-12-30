package com.ultimatepolish.db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	private static final Class<?>[] classes = new Class[] {
	    Player.class, PlayerStats.class, Game.class, Throw.class, SessionMember.class, Badge.class, Session.class, Venue.class
	};
	public static void main(String[] args) throws Exception {
		writeConfigFile("ormlite_config.txt", classes);
	}
}
