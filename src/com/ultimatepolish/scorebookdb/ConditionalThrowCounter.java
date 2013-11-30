package com.ultimatepolish.scorebookdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConditionalThrowCounter {

	public static HashMap<Integer, HashMap<Integer,Integer>> count(
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
	
	public static HashMap<Integer, HashMap<Integer,Double>> countsToFractions(
			HashMap<Integer, HashMap<Integer,Integer>> counts){
		HashMap<Integer, HashMap<Integer,Double>> m = new HashMap<Integer, HashMap<Integer,Double>>();
		HashMap<Integer, Integer> summedCounts = collapse(counts);
		
		Double f;
		for ( Integer i : counts.keySet()){
			m.put(i, new HashMap<Integer, Double>());
			for ( Integer j : counts.get(i).keySet()){
				f = divideIntegers(counts.get(i).get(j), summedCounts.get(i));
				m.get(i).put(j, f);
			}
		}
		return m;
	}
	
	public static HashMap<Integer, Integer> collapse(
			HashMap<Integer, HashMap<Integer,Integer>> nestedMap){
		HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
		for ( Integer i : nestedMap.keySet()) {
			m.put(i, sumMap(nestedMap.get(i)));
		}
		return m;
	}
	
	public static Integer sumMap(HashMap<Integer,Integer> m){
		Integer s = 0;
		for (Integer i : m.values()) {
			s+=i;
		}
		return s;
	}
	
	public static double divideIntegers(Integer num, Integer denom){
		if (denom==1){
			return 0.0;
		}
		return (double)num/(double)denom;
	}
	
	private static HashMap<Integer, HashMap<Integer,Integer>> initializeCountMap(
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
