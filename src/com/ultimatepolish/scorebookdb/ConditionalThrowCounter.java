package com.ultimatepolish.scorebookdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConditionalThrowCounter {

	public HashMap<Integer, HashMap<Integer,Integer>> count(
			ArrayList<Throw> tArray, 
			ThrowIndicator condition, 
			Map<Integer, ThrowIndicator> outcomeIndicators){
		HashMap<Integer, HashMap<Integer,Integer>> m;
		
		//m initialized to have 0's in all potentially valid buckets
		m = initializeCountMap(condition, outcomeIndicators);
		
		int i,j,n;
		for (Throw t : tArray) {
			i = condition.indicate(t);
			j = outcomeIndicators.get(i).indicate(t);
			n = m.get(i).get(j);
			m.get(i).put(j, n+1);
		}
		return m;
	}
	
	private HashMap<Integer, HashMap<Integer,Integer>> initializeCountMap(
			ThrowIndicator condition, 
			Map<Integer, ThrowIndicator> outcomeIndicators){
		
		ArrayList<Integer> outerCategories = condition.categories();
		ArrayList<Integer> innerCategories;
		HashMap<Integer, HashMap<Integer,Integer>> m;
		HashMap<Integer,Integer> outcomeMap;
		m = new HashMap<Integer, HashMap<Integer,Integer>>();

		for (Integer i : outerCategories) {
			m.put(i,new HashMap<Integer, Integer>());
			innerCategories = outcomeIndicators.get(i).categories();
			for (Integer j : innerCategories) {
				m.get(i).put(j, 0);
			}
		}
		
		return m;
	}
	
		
}
