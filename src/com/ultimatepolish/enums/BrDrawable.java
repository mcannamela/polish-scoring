package com.ultimatepolish.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ultimatepolish.polishscorebook.R;

/** Enum for the nodes in a bracket */
public final class BrDrawable {
	// These are the different types. Negative numbers used to avoid conflict
	// with seed numbers.
	public static final int UP = R.drawable.bracket_top;
	public static final int LW = R.drawable.bracket_bottom;
	public static final int UP_EL = R.drawable.bracket_top_eliminated;
	public static final int LW_EL = R.drawable.bracket_bottom_eliminated;
	public static final int UP_LA = R.drawable.bracket_top_labeled;
	public static final int LW_LA = R.drawable.bracket_bottom_labeled;
	public static final int UP_EL_LA = R.drawable.bracket_top_eliminated_labeled;
	public static final int LW_EL_LA = R.drawable.bracket_bottom_eliminated_labeled;
	public static final int END = R.drawable.bracket_endpoint;

	public static final Map<String, Integer> map;
	static {
		Map<String, Integer> tempMap = new HashMap<String, Integer>();
		tempMap.put("upper", UP);
		tempMap.put("lower", LW);
		tempMap.put("upper_eliminated", UP_EL);
		tempMap.put("lower_eliminated", LW_EL);
		tempMap.put("upper_labeled", UP_LA);
		tempMap.put("lower_labeled", LW_LA);
		tempMap.put("upper_eliminated_labeled", UP_EL_LA);
		tempMap.put("lower_eliminated_labeled", LW_EL_LA);
		tempMap.put("endpoint", END);
		map = Collections.unmodifiableMap(tempMap);
	}
}