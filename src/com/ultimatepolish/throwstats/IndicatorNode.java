package com.ultimatepolish.throwstats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.ultimatepolish.scorebookdb.Throw;

public class IndicatorNode {

	ThrowIndicator indicator = null;
	IndicatorNode parent = null;
	HashMap<String, IndicatorNode> childMap = null;
	String key = null;
	
	public int count = 0;

	public IndicatorNode(ThrowIndicator indicator) {
		this.indicator = indicator;
	}
	
	public void addChild(String key, IndicatorNode node){
		if (!indicator.enumerate().contains(key)){
			throw new RuntimeException("key must be a member of indicator.enumerate()");
		}
		childMap.put(key, node);
		node.parent = this;
		node.key = key;
		
	}
	
	public Set<String> childKeys(){
		return indicator.enumerate();
	}
	
	public int parentCount(){
		if (isRoot()){
			return count;
		}
		return parent.count;
	}
	
	public double fraction(){
		return divideIntegers(count, parentCount());
	}
	
	public double absoluteFraction(){
		if (isRoot()){
			return 1;
		}
		return fraction()*parent.absoluteFraction();
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
		return (indicator==null && childMap==null);
	}
	
	public boolean isRoot(){
		return parent==null;
	}
	
	public boolean isComplete(){
		return childMap.keySet().equals(indicator.enumerate());
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
