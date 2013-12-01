/**
 * 
 */
package com.ultimatepolish.scorebookdb;

import java.util.ArrayList;

/**
 * This class provides one method, which maps a throw to an integer. 
 * Should be used to e.g. count whether a throw meets some certain criterion, 
 * like being either a bottle, pole, or cup hit. 
 * 
 *
 */
public interface ThrowIndicator {
	
	public int indicate(Throw t);
	
	public ArrayList<Integer> categories();

}
