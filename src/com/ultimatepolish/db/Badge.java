package com.ultimatepolish.db;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Badge{
	
	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField(foreign = true)
	private Player player;
	
	@DatabaseField(foreign = true)
	private Session session;
	
	@DatabaseField(canBeNull=false)
	private int badgeType;
	
	Badge(){}

	public Badge(Player player, Session session, int badgeType) {
		super();
		this.player = player;
		this.session = session;
		this.badgeType = badgeType;
	}
	
	public Badge(Player player, int badgeType) {
		super();
		this.player = player;
		this.badgeType = badgeType;
	}
	
	public static Dao<Badge, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Badge, Long> d = helper.getBadgeDao();
		return d;
	}
		
	public static List<Badge> getAll(Context context) throws SQLException{
		Dao<Badge, Long> d = Badge.getDao(context);
		List<Badge> badges = new ArrayList<Badge>();
		for(Badge b:d){
			badges.add(b);
		}
		return badges;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
}
