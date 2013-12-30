package com.ultimatepolish.enums;

import java.util.HashMap;
import java.util.Map;

import com.ultimatepolish.rulesets.RuleSet;
import com.ultimatepolish.rulesets.RuleSet00;
import com.ultimatepolish.rulesets.RuleSet01;

public final class RuleType {
	public static final RuleSet RS00 = new RuleSet00();
	public static final RuleSet RS01 = new RuleSet01();

	public static final int rsNull = -1; // for sessions, no default rule set
	public static final int rs00 = 0;
	public static final int rs01 = 1;

	public static final Map<Integer, RuleSet> map = new HashMap<Integer, RuleSet>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put(0, new RuleSet00());
			put(1, new RuleSet01());
		}
	};
}
