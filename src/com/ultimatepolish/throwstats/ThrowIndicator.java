/**
 * 
 */
package com.ultimatepolish.throwstats;

import java.util.Set;

import com.ultimatepolish.db.Throw;

/**
 * This class provides one method, which maps a throw to an string. 
 * Should be used to e.g. count whether a throw meets some certain criterion, 
 * like being either a bottle, pole, or cup hit. 
 * 
 *
 */
public interface ThrowIndicator {
	
	public String indicate(Throw t);
	
	public Set<String> enumerate();

}
