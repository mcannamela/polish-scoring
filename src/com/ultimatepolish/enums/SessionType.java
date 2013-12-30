package com.ultimatepolish.enums;

public final class SessionType {
	public static final int OPEN = 0;
	public static final int LEAGUE = 1;
	public static final int LADDER = 2;
	//leave some room for other non-tourney types to be added later
	public static final int SNGL_ELIM = 10;
	public static final int DBL_ELIM = 11;

    public static final String[] typeString = {"Open", "League", "Ladder", "", "", "", "", "", "", "",
    	"Single-elimination Tournament", "Double-elimination Tournament"};
}
