package com.ultimatepolish.scorebookdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ultimatepolish.scorebookdb.enums.ThrowResult;
import com.ultimatepolish.scorebookdb.enums.ThrowType;
import com.ultimatepolish.scorebookdb.rulesets.RuleSet;

public class SimpleThrowStats {
	private ArrayList<Throw> tArray;
	
	public HashMap<String, Double> offensiveStats = new HashMap<String, Double>(); 
	
	private static final HashMap<Integer, ThrowIndicator> offensiveIndicatorMap = initializeOffensiveCountingMaps();
	private static final BallStrikeHitCondition offensiveCondition = new BallStrikeHitCondition();
	
	private static final String BALL_RATE = "BallRate";
	private static final String STRIKE_RATE = "StrikeRate";
	private static final String HIT_RATE = "HitRate";
	
	
	public SimpleThrowStats(ArrayList<Throw> tArray) {
		super();
		this.tArray = tArray;
	}
	
	public void computeOffensiveStats() {
		HashMap<Integer, HashMap<Integer,Integer>> c = getOffensiveCounts();
		HashMap<Integer, HashMap<Integer,Double>> f = ConditionalThrowCounter.countsToFractions(c);
		
		HashMap<Integer,Integer> summedCounts = ConditionalThrowCounter.collapse(c);
		Integer totalCounts = ConditionalThrowCounter.sumMap(summedCounts);
		HashMap<Integer,Double> marginalFractions = new HashMap<Integer,Double>();
		
		double x;
		
		for (Integer i : summedCounts.keySet()) {
			x = ConditionalThrowCounter.divideIntegers(summedCounts.get(i), totalCounts);
			marginalFractions.put(i, x);
		}
		
		offensiveStats.put(BALL_RATE, marginalFractions.get(offensiveCondition.BALL));
		offensiveStats.put(STRIKE_RATE, marginalFractions.get(offensiveCondition.STRIKE));
		offensiveStats.put(HIT_RATE, marginalFractions.get(offensiveCondition.HIT));
				
	}
	
	public HashMap<Integer, HashMap<Integer,Integer>> getOffensiveCounts(){
		return ConditionalThrowCounter.count(tArray, offensiveCondition, offensiveIndicatorMap);
	}
	
	private static HashMap<Integer, ThrowIndicator> initializeOffensiveCountingMaps(){
		HashMap<Integer, ThrowIndicator> m = new HashMap<Integer, ThrowIndicator>();
		
		MarginalCondition c = new MarginalCondition();
		BallIndicator bi = new BallIndicator();
		PoleCupBottleIndicator pcb = new PoleCupBottleIndicator();
		
		m.put(BallStrikeHitCondition.BALL, bi);
		m.put(BallStrikeHitCondition.STRIKE, c);
		m.put(BallStrikeHitCondition.OTHER, c);
		m.put(BallStrikeHitCondition.HIT, pcb);
		return m;
	}
	
	/*
	 * Lump all throws together
	 */
	static class MarginalCondition implements ThrowIndicator{

		@Override
		public int indicate(Throw t) {
			return 0;
		}

		@Override
		public ArrayList<Integer> categories() {
			return (ArrayList<Integer>) Arrays.asList(0);
		}
	}
	
	/*
	 * Separate into balls, strikes, and stack hits
	 */
	static class BallStrikeHitCondition implements ThrowIndicator{
		public static final int BALL = 0;
		public static final int STRIKE = 1;
		public static final int HIT = 2;
		public static final int OTHER = 3;
		
		@Override
		public int indicate(Throw t) {
			
			if (t.isStackHit()){
				return HIT;
			}
			if (t.isBall()){
				return BALL;
			}
			if (t.isStrike()){
				return STRIKE;
			}
			return OTHER;
		}

		@Override
		public ArrayList<Integer> categories() {
			return (ArrayList<Integer>) Arrays.asList(BALL, STRIKE, HIT, OTHER);
		}
	}
	
	/*
	 * Separate balls into HRLL
	 */
	static class BallIndicator implements ThrowIndicator {
		public static final int HIGH = 0;
		public static final int RIGHT = 1;
		public static final int LOW = 2;
		public static final int LEFT = 3;
		public static final int OTHER = 4;
		
		@Override
		public int indicate(Throw t) {
			if (t.throwType==ThrowType.BALL_HIGH){
				return HIGH;
			}
			if (t.throwType==ThrowType.BALL_LOW){
				return LOW;
			}
			if (t.throwType==ThrowType.BALL_RIGHT){
				return RIGHT;
			}
			if (t.throwType==ThrowType.BALL_LEFT){
				return LEFT;
			}
			return OTHER;
		}
		
		@Override
		public ArrayList<Integer> categories() {
			return (ArrayList<Integer>) Arrays.asList(HIGH, RIGHT, LOW, LEFT, OTHER);
		}
	}
	
	/*
	 * Separate by defensive results
	 */
	static class DropCatchStalwartIndicator implements ThrowIndicator{
		public static final int DROP = 0;
		public static final int CATCH = 1;
		public static final int STALWART = 2;
		public static final int OTHER = 3;
		
		@Override
		public int indicate(Throw t) {
			if (t.throwResult==ThrowResult.DROP){
				return DROP;
			}
			if (t.throwResult==ThrowResult.CATCH){
				return CATCH;
			}
			if (t.throwResult==ThrowResult.STALWART){
				return STALWART;
			}
			return OTHER;
		}
		
		@Override
		public ArrayList<Integer> categories() {
			return (ArrayList<Integer>) Arrays.asList(OTHER, DROP, CATCH, STALWART);
		}
	}
	/*
	 * Separate by type of hit
	 */
	static class PoleCupBottleIndicator implements ThrowIndicator{
		public static final int POLE = 0;
		public static final int CUP = 1;
		public static final int BOTTLE= 2;
		public static final int OTHER = 3;
		
		@Override
		public int indicate(Throw t) {
			if (t.throwType==ThrowType.POLE){
				return POLE;
			}
			if (t.throwType==ThrowType.CUP){
				return CUP;
			}
			if (t.throwType==ThrowType.BOTTLE){
				return BOTTLE;
			}
			return OTHER;
		}
		
		@Override
		public ArrayList<Integer> categories() {
			return (ArrayList<Integer>) Arrays.asList(OTHER, POLE,CUP,BOTTLE);
		}
	}
	
	
}