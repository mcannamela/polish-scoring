package com.ultimatepolish.throwstats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.util.Log;

import com.ultimatepolish.db.Throw;
import com.ultimatepolish.enums.ThrowResult;
import com.ultimatepolish.enums.ThrowType;

public class SimpleThrowStats {
	public static String LOGTAG = "ThrowStats";
	private List<Throw> tArray;
	
	private static ThrowIndicator leafIndicator = new LeafIndicator();
	private static ThrowIndicator ballStrikeHitIndicator = new BallStrikeHitIndicator();
	private static ThrowIndicator ballIndicator = new BallIndicator();
	private static ThrowIndicator poleCupBottleIndicator = new PoleCupBottleIndicator();
	private static ThrowIndicator dropCatchStalwartIndicator = new DropCatchStalwartIndicator();
	
	
	
	public SimpleThrowStats(List<Throw> tArray) {
		this.tArray = tArray;
	}
	
	public IndicatorNode computeStats(){
		CountVisitor v = new CountVisitor();
		IndicatorNode root = buildTree();
		log("Will now process "+tArray.size() +" throws");
		int cnt = 0;
		for (Throw  t: tArray) {
//			if (cnt%10==0){
//				log("Processing throw "+cnt+" of "+tArray.size());
//			}
			v.setThrow(t);
			v.visit(root);
			cnt++;
			
		}
		return root;
	}
	
	private static IndicatorNode leafNode(){
		return new IndicatorNode(leafIndicator);
	}
	
	private static void fillLeaves(IndicatorNode node){
		for (String k : node.childKeys()){
			node.addChild(k, leafNode());
		}
	}
	
	private static IndicatorNode buildTree(){
		IndicatorNode root = new IndicatorNode(ballStrikeHitIndicator);
		IndicatorNode ballNode, strikeNode, hitNode, poleNode, cupNode, bottleNode;
		
		ballNode = new IndicatorNode(ballIndicator);
		strikeNode = new IndicatorNode(dropCatchStalwartIndicator);
		
		hitNode = new IndicatorNode(poleCupBottleIndicator);
		cupNode = new IndicatorNode(dropCatchStalwartIndicator);
		poleNode = new IndicatorNode(dropCatchStalwartIndicator);
		bottleNode = new IndicatorNode(dropCatchStalwartIndicator);
		
		root.addChild(BallStrikeHitIndicator.BALL, ballNode);
		root.addChild(BallStrikeHitIndicator.STRIKE, strikeNode);
		root.addChild(BallStrikeHitIndicator.HIT, hitNode);
		root.addChild(BallStrikeHitIndicator.OTHER, leafNode());
		
		fillLeaves(ballNode);
		fillLeaves(strikeNode);
		
		hitNode.addChild(PoleCupBottleIndicator.POLE, poleNode);
		hitNode.addChild(PoleCupBottleIndicator.CUP, cupNode);
		hitNode.addChild(PoleCupBottleIndicator.BOTTLE, bottleNode);
		hitNode.addChild(PoleCupBottleIndicator.OTHER, leafNode());
		
		fillLeaves(poleNode);
		fillLeaves(cupNode);
		fillLeaves(bottleNode);
		
		ValidationVisitor v = new ValidationVisitor();
		v.visit(root);
		
		return root;
	}
	
	/*
	 * Lump all throws together
	 */
	static class LeafIndicator implements ThrowIndicator{

		@Override
		public String indicate(Throw t) {
			return "NULL";
		}

		@Override
		public Set<String> enumerate() {
			return new HashSet<String>();
		}
	}
	
	/*
	 * Separate into balls, strikes, and stack hits
	 */
	static class BallStrikeHitIndicator extends BaseThrowIndicator{
		public static final String BALL = "Ball";
		public static final String STRIKE = ThrowType.getString(ThrowType.STRIKE);
		public static final String HIT = "StackHit";
		public static final String OTHER = "Other";
		
		public BallStrikeHitIndicator(){
			keys.add(BALL);
			keys.add(STRIKE);
			keys.add(HIT);
			keys.add(OTHER);
		}
		
		@Override
		public String indicate(Throw t) {
			
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
	}
	
	/*
	 * Separate balls into HRLL
	 */
	static class BallIndicator extends BaseThrowIndicator {
		public static final String HIGH = ThrowType.getString(ThrowType.BALL_HIGH);
		public static final String RIGHT = ThrowType.getString(ThrowType.BALL_RIGHT);
		public static final String LOW = ThrowType.getString(ThrowType.BALL_LOW);
		public static final String LEFT = ThrowType.getString(ThrowType.BALL_LEFT);
		public static final String OTHER = "Other";
		
		public BallIndicator(){
			keys.add(HIGH);
			keys.add(RIGHT);
			keys.add(LOW);
			keys.add(LEFT);
			keys.add(OTHER);
		}
		
		@Override
		public String indicate(Throw t) {
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
		
	}
	
	/*
	 * Separate by type of hit
	 */
	static class PoleCupBottleIndicator extends BaseThrowIndicator{
		public static final String POLE = ThrowType.getString(ThrowType.POLE);
		public static final String CUP = ThrowType.getString(ThrowType.CUP);
		public static final String BOTTLE = ThrowType.getString(ThrowType.BOTTLE);
		public static final String OTHER = "Other";
		
		public PoleCupBottleIndicator(){
			keys.add(POLE);
			keys.add(CUP);
			keys.add(BOTTLE);
			keys.add(OTHER);
		}
		
		@Override
		public String indicate(Throw t) {
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
		
	}
	
	/*
	 * Separate by defensive results
	 */
	static class DropCatchStalwartIndicator extends BaseThrowIndicator{
		public static final String DROP = ThrowResult.getString(ThrowResult.DROP);
		public static final String CATCH = ThrowResult.getString(ThrowResult.CATCH);
		public static final String STALWART = ThrowResult.getString(ThrowResult.STALWART);
		public static final String OTHER = "Other";
		
		public DropCatchStalwartIndicator(){
			keys.add(DROP);
			keys.add(CATCH);
			keys.add(STALWART);
			keys.add(OTHER);
		}
		
		@Override
		public String indicate(Throw t) {
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
		
	}
	
	public void log(String msg) {
		Log.i(LOGTAG, msg);
	}
	
}