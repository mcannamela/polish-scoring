package com.ultimatepolish.scorebookdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.ultimatepolish.scorebookdb.enums.ThrowResult;
import com.ultimatepolish.scorebookdb.enums.ThrowType;
import com.ultimatepolish.scorebookdb.rulesets.RuleSet;

public class SimpleThrowStats {
	private ArrayList<Throw> tArray;
	/*
	 * Lump all throws together to get marginal hit/ball rates
	 */
	class MarginalCondition implements ThrowIndicator{

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
	class BallStrikeHitCondition implements ThrowIndicator{
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
	class BallIndicator implements ThrowIndicator {
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
	class DropCatchStalwartIndicator implements ThrowIndicator{
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
	class PoleCupBottleIndicator implements ThrowIndicator{
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