package com.ultimatepolish.throwstats;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseThrowIndicator implements ThrowIndicator {

	protected Set<String> keys = new HashSet<String>();
	
	@Override
	public Set<String> enumerate() {
		return keys;
	}
	

}
