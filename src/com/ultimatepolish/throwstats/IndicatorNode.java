package com.ultimatepolish.throwstats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ultimatepolish.db.Throw;

public class IndicatorNode {

	ThrowIndicator indicator = null;
	IndicatorNode parent = null;
	HashMap<String, IndicatorNode> childMap = new HashMap<String, IndicatorNode>();
	String key = null;
	
	public static double proportionStandardDev(double p, int n){
		if (n==0){
			return 0.0;
		}
		return Math.sqrt(p*(1-p)/(double) n);
	}
	
	public int count = 0;

	public IndicatorNode(ThrowIndicator indicator) {
		this.indicator = indicator;
	}
	
	public void addChild(String key, IndicatorNode node){
		if (!indicator.enumerate().contains(key)){
			String msg = "key must be a member of indicator.enumerate(): "+key+" not in ";
			for (String allowedKey : indicator.enumerate()) {
				msg+= allowedKey+", ";
			}
			throw new RuntimeException(msg);
		}
		childMap.put(key, node);
		node.parent = this;
		node.key = key;
		
	}
	
	public Set<String> childKeys(){
		if (indicator==null){
			return new HashSet<String>();
		}
		return indicator.enumerate();
	}
	
	public int parentCount(){
		if (isRoot()){
			return count;
		}
		return parent.count;
	}
	
	public int rootCount(){
		if (isRoot()){
			return count;
		}
		return parent.rootCount();
	}
	
	public double conditionalFraction(){
		return divideIntegers(count, parentCount());
	}
	
	/*
	 * Estimate standard dev with normal approximation
	 */
	public double conditionalFractionStandardDev(){
		return proportionStandardDev(conditionalFraction(), parentCount());
	}
	
	
	public double absoluteFraction(){
		if (isRoot()){
			return 1;
		}
		return conditionalFraction()*parent.absoluteFraction();
	}
	
	public double absoluteFractionStandardDev(){
		return proportionStandardDev(absoluteFraction(), rootCount());
	}
	
	public ArrayList<String> lineage(){
		if (isRoot()){
			return new ArrayList<String>();
		}
		ArrayList<String> arr = parent.lineage();
		arr.add(key);
		return arr;
	}
	
	public boolean isLeaf(){
		return (childKeys().size()==0);
	}
	
	public boolean isRoot(){
		return parent==null;
	}
	
	public boolean isComplete(){
		return childMap.keySet().equals(indicator.enumerate());
	}
	
	public Set<String> getChildMapKeySet(){
		return childMap.keySet();
	}
	
	public void increment(int inc){
		count+=inc;
	}
	
	public void increment(){
		increment(1);
	}
	
	public IndicatorNode indicate(Throw t){
		String key = indicator.indicate(t);
		return childMap.get(key);
	}


	public static double divideIntegers(Integer num, Integer denom){
		if (denom==0){
			return 0.0;
		}
		return (double)num/(double)denom;
	}
	
}
