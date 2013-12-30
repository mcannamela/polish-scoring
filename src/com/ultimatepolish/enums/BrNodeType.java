package com.ultimatepolish.enums;

/** Enum for the nodes in a bracket */
public final class BrNodeType {
	// These are the different types
	public static final int TIP = 0;
	public static final int WIN = 1;
	public static final int LOSS = 2;
	public static final int BYE = 3;
	public static final int UNSET = 4;
	public static final int RESPAWN = 5;
	public static final int NA = 7;

	// These are used to modify viewIds for easy classification
	public static final int UPPER = 1000;
	public static final int LOWER = 2000;
	public static final int U2L = 1000;
	public static final int L2U = -1000;
	public static final int MOD = 1000;

	public static final String[] typeString = { "Tip", "Win", "Loss", "Bye",
			"Unset", "Respawn", "NA" };
}