package com.ultimatepolish.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Enum for the nodes in a bracket */
public final class BrNodeType {
	// These are the different types. Negative numbers used to avoid conflict
	// with seed numbers.
	public static final int TIP = -1;
	public static final int WIN = -2;
	public static final int LOSS = -3;
	public static final int BYE = -4;
	public static final int UNSET = -5;
	public static final int RESPAWN = -6;
	public static final int NA = -7;

	// These are used to modify viewIds for easy classification
	public static final int UPPER = 1000;
	public static final int LOWER = 2000;
	public static final int U2L = 1000;
	public static final int L2U = -1000;
	public static final int MOD = 1000;

	public static final Map<Integer, String> map;
	static {
		Map<Integer, String> tempMap = new HashMap<Integer, String>();
		tempMap.put(TIP, "Tip");
		tempMap.put(WIN, "Win");
		tempMap.put(LOSS, "Loss");
		tempMap.put(BYE, "Bye");
		tempMap.put(UNSET, "Unset");
		tempMap.put(RESPAWN, "Respawn");
		tempMap.put(NA, "N/A");
		map = Collections.unmodifiableMap(tempMap);
	}
}