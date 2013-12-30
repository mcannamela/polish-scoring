package com.ultimatepolish.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Player implements Comparable<Player> {
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String NICK_NAME = "nickName";
	public static final String IS_ACTIVE = "isActive";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(columnName = FIRST_NAME, canBeNull = false, uniqueCombo = true)
	private String firstName;

	@DatabaseField(columnName = LAST_NAME, canBeNull = false, uniqueCombo = true)
	private String lastName;

	@DatabaseField(columnName = NICK_NAME, canBeNull = false, uniqueCombo = true)
	private String nickName;

	@DatabaseField
	public boolean throwsRightHanded;

	@DatabaseField
	public boolean throwsLeftHanded;

	@DatabaseField
	public boolean prefersRightSide;

	@DatabaseField
	public boolean prefersLeftSide;

	@DatabaseField
	private int height_cm;

	@DatabaseField
	private int weight_kg;

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	byte[] imageBytes;

	@DatabaseField
	public int color;

	@DatabaseField
	private boolean isActive = true;

	Player() {
	}

	public Player(String firstName, String lastName, String nickName,
			boolean throwsRightHanded, boolean throwsLeftHanded,
			boolean prefersRightSide, boolean prefersLeftSide, int height_cm,
			int weight_kg, byte[] imageBytes, int color) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.nickName = nickName;
		this.throwsRightHanded = throwsRightHanded;
		this.throwsLeftHanded = throwsLeftHanded;
		this.prefersRightSide = prefersRightSide;
		this.prefersLeftSide = prefersLeftSide;
		this.height_cm = height_cm;
		this.weight_kg = weight_kg;
		this.imageBytes = imageBytes;
		this.color = color;
	}

	public static Dao<Player, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Player, Long> d = null;
		try {
			d = helper.getPlayerDao();
		} catch (SQLException e) {
			throw new RuntimeException("Couldn't get player dao: ", e);
		}
		return d;
	}

	public static long getIdByNames(String first, String last, String nick,
			Context context) throws SQLException {
		Player p = getByNames(first, last, nick, context);
		if (p == null) {
			return -1;
		}
		return p.getId();
	}

	public static Player getByNames(String first, String last, String nick,
			Context context) throws SQLException {
		List<Player> pList = null;
		HashMap<String, Object> m = buildNameMap(first, last, nick);

		pList = getDao(context).queryForFieldValuesArgs(m);
		if (pList.isEmpty()) {
			return null;
		} else {
			return pList.get(0);
		}
	}

	public static boolean exists(String first, String last, String nick,
			Context context) throws SQLException {
		if (first == null || last == null || nick == null) {
			return false;
		}
		List<Player> pList = null;
		HashMap<String, Object> m = buildNameMap(first, last, nick);

		pList = getDao(context).queryForFieldValuesArgs(m);
		if (pList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean exists(Context context) throws SQLException {
		return exists(firstName, lastName, nickName, context);
	}

	public static List<Player> getAll(Context context) throws SQLException {
		Dao<Player, Long> d = Player.getDao(context);
		List<Player> players = new ArrayList<Player>();
		for (Player p : d) {
			players.add(p);
		}
		return players;
	}

	public static HashMap<String, Object> buildNameMap(String first,
			String last, String nick) {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put(FIRST_NAME, first.toLowerCase(Locale.US));
		m.put(LAST_NAME, last.toLowerCase(Locale.US));
		m.put(NICK_NAME, nick.toLowerCase(Locale.US));
		return m;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisplayName() {
		return firstName + " \"" + nickName + "\" " + lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public boolean getRightHanded() {
		return throwsRightHanded;
	}

	public void setRightHanded(boolean rightHanded) {
		this.throwsRightHanded = rightHanded;
	}

	public boolean getLeftHanded() {
		return throwsLeftHanded;
	}

	public void setLeftHanded(boolean leftHanded) {
		this.throwsLeftHanded = leftHanded;
	}

	public boolean getPrefersRightSide() {
		return prefersRightSide;
	}

	public void setPrefersRightSide(boolean prefersRightSide) {
		this.prefersRightSide = prefersRightSide;
	}

	public boolean getPrefersLeftSide() {
		return prefersLeftSide;
	}

	public void setPrefersLeftSide(boolean prefersLeftSide) {
		this.prefersLeftSide = prefersLeftSide;
	}

	public int getHeight_cm() {
		return height_cm;
	}

	public void setHeight_cm(int height_cm) {
		this.height_cm = height_cm;
	}

	public int getWeight_kg() {
		return weight_kg;
	}

	public void setWeight_kg(int weight_kg) {
		this.weight_kg = weight_kg;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public int compareTo(Player another) {
		if (id < another.id) {
			return -1;
		} else if (id == another.id) {
			return 0;
		} else {
			return 1;
		}
	}

	public boolean equals(Object o) {
		if (!(o instanceof Player))
			return false;
		Player another = (Player) o;
		if (id == another.id) {
			return true;
		} else {
			return false;
		}
	}
}
