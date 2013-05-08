package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.Date;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.ultimatepolish.polishscorebook.R;

public class Throw implements Comparable<Throw>{
	public static final String GAME_ID = "gameId";
	public static final String THROW_NUMBER = "throwNumber";
	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private int throwNumber;
	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private long gameId;
	@DatabaseField(canBeNull=false)
	private long playerId;

	@DatabaseField(canBeNull=false)
	private Date timestamp;
	@DatabaseField(canBeNull=false)
	private int throwType;
	@DatabaseField(canBeNull=false)
	private int throwResult;
	
	@DatabaseField
	public boolean isError = false;
	@DatabaseField
	private int errorScore = 0;
	
	@DatabaseField
	public boolean isOwnGoal = false;
	@DatabaseField
	private int ownGoalScore= 0;
	
	@DatabaseField
	public boolean isGoaltend = false;
	@DatabaseField
	private int goaltendScore= 0;
		
	@DatabaseField
	public boolean isDrinkHit = false;
	@DatabaseField
	public boolean isDrinkDropped = false;
	
	@DatabaseField
	public boolean isTrap = false;
	
	
	@DatabaseField
	public boolean isOnFire = false;
	@DatabaseField
	public boolean isFiredOn = false;
	
	@DatabaseField
	public boolean isShort = false;
	
	@DatabaseField
	public boolean isBroken = false;
	
	@DatabaseField
	private int initialOffensivePlayerScore = 0;
	@DatabaseField
	private int initialDefensivePlayerScore = 0;
	
	Throw(){}
	
	public Throw(int throwNumber, long gameId, long playerId, Date timestamp,
			int throwType, int throwResult) {
		super();
		this.throwNumber = throwNumber;
		this.gameId = gameId;
		this.playerId = playerId;
		this.timestamp = timestamp;
		this.throwType = throwType;
		this.throwResult = throwResult;
	}
	

	public Throw(int throwNumber, long gameId, long playerId, Date timestamp) {
		super();
		this.throwNumber = throwNumber;
		this.gameId = gameId;
		this.playerId = playerId;
		this.timestamp = timestamp;
	}
	public static Dao<Throw, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Throw, Long> d = helper.getThrowDao();
		return d;
	}
	public void setInitialScores(Throw previousThrow){
		int[] scores = previousThrow.getFinalScores();
		setInitialDefensivePlayerScore(scores[0]);
		setInitialOffensivePlayerScore(scores[1]);
	}
	public void setInitialScores(){
		setInitialDefensivePlayerScore(0);
		setInitialOffensivePlayerScore(0);
	}
	public int[] getFinalScores(){
		int[] inc = getScoreIncrements();
		int[] finalScores = {initialOffensivePlayerScore, 
				initialDefensivePlayerScore};
		for (int i=0;i<2;i++){
			finalScores[i]+=inc[i];
		}
		return finalScores;
	}
	private int[] getScoreIncrements(){
		int[] inc = {0,0};
		switch (throwResult){
			case ThrowResult.CATCH:
				break;
			case ThrowResult.DROP:
				switch (throwType){
					case ThrowType.STRIKE:
						if (!isDropScoreBlocked()){
							inc[0]=1;
						}
						break;
					case ThrowType.POLE: 
					case ThrowType.CUP:
						inc[0] = 2;
						break;
					case ThrowType.BOTTLE:
						inc[0] = 3;
					default:
						break;
				}
				break;
			case ThrowResult.STALWART:
				switch(throwType){
					case ThrowType.POLE: 
					case ThrowType.CUP:
					case ThrowType.BOTTLE: 
						inc[1] = 1;
						break;
					default:
						break;
				}
				
				break;
		}
		
		if (isDrinkDropped){
			inc[1]-=1;
		}
		if (isDrinkHit){
			inc[1]-=1;
		}
		if (isTrap){
			inc[0]-=1;
		}
		if (isOwnGoal){
			inc[1]+= ownGoalScore;
		}
		if (isError){
			inc[0]+= errorScore;
		}
		if (isGoaltend){
			inc[0]+= goaltendScore;
		}
		
		if (isBroken){
			inc[0] = 20;
			inc[1] = 0;
		}
		
		return inc;
	}
	private boolean isDropScoreBlocked(){
		boolean isBlocked = false;
		int oScore = initialOffensivePlayerScore;
		int dScore = initialDefensivePlayerScore;
		if (oScore>=10 && dScore<oScore){
			isBlocked = true;
		}
		return isBlocked;
	}
	public String getSpecialString(){
		String s = "";
		if(isError){
			s+="e"+String.valueOf(errorScore);
		}
		if (isOwnGoal){
			s+="o"+String.valueOf(ownGoalScore);
		}
		if (isGoaltend){
			s+="g"+String.valueOf(goaltendScore);
		}
		if (isOnFire){
			s+="f";
		}
		if (isFiredOn){
			s+="F";
		}
		if (isBroken){
			s+="*";
		}
		if (isTrap){
			s+="^";
		}
		if (isShort){
			s+="v";
		}
		if (isDrinkDropped){
			s+="d";
		}
		if (isDrinkHit){
			s+="d";
		}
		if (s.length()==0){
			s = "--";
		}
		return s;
	}
//	public String getThrowString(){
//		String s = "";
//		switch(throwType){
//			case ThrowType.BALL_HIGH:
//				s = "^";
//				break;
//			case ThrowType.BALL_LEFT:
//				s = "<";
//				break;
//			case ThrowType.BALL_RIGHT:
//				s = ">";
//				break;
//			case ThrowType.BALL_LOW:
//				s = "v";
//				break;
//			case ThrowType.STRIKE:
//				switch (throwResult){
//				case ThrowResult.DROP:
//					s = "o";
//					break;
//				case ThrowResult.CATCH:
//				case ThrowResult.STALWART:
//					s = "\u00b7";
//					break;
//				}
//				break;
//			case ThrowType.POLE:
//				switch (throwResult){
//				case ThrowResult.DROP:
//					s = "\u03a6";
//					break;
//				case ThrowResult.CATCH:
//					s = "|";
//					break;
//				case ThrowResult.STALWART:
//					s = "+";
//					break;
//				}
//				break;
//			case ThrowType.CUP:
//				switch (throwResult){
//				case ThrowResult.DROP:
//					s = "\u00a9";
//					break;
//				case ThrowResult.CATCH:
//					s = "\u20b5";
//					break;
//				case ThrowResult.STALWART:
//					s = "\u20b5"+"+";
//					break;
//				}
//				break;
//			case ThrowType.BOTTLE:
//				switch (throwResult){
//					case ThrowResult.DROP:
//						s = "@";
//						break;
//					case ThrowResult.CATCH:
//						s = "\u0394";
//						break;
//					case ThrowResult.STALWART:
//						s = "\u0394"+"+";
//						break;
//				}
//				break;
//		}
//		return s;
//	}
	public int getThrowDrawableId(){
		int d = R.drawable.bxs_notthrown;
		switch(throwType){
			case ThrowType.BALL_HIGH:
				if (isOnFire) {
					d = R.drawable.bxs_high_fire;
				} else {
					d = R.drawable.bxs_high;
				}
				break;
			case ThrowType.BALL_LEFT:
				if (isOnFire) {
					d = R.drawable.bxs_left_fire;
				} else {
					d = R.drawable.bxs_left;
				}
				break;
			case ThrowType.BALL_RIGHT:
				if (isOnFire) {
					d = R.drawable.bxs_right_fire;
				} else {
					d = R.drawable.bxs_right;
				}
				break;
			case ThrowType.BALL_LOW:
				if (isOnFire) {
					d = R.drawable.bxs_low_fire;
				} else {
					d = R.drawable.bxs_low;
				}
				break;
			case ThrowType.STRIKE:
				switch (throwResult){
				case ThrowResult.DROP:
					d = R.drawable.bxs_strike_drop;
					break;
				case ThrowResult.CATCH:
				case ThrowResult.STALWART:
					d = R.drawable.bxs_strike_catch;
					break;
				}
				break;
			case ThrowType.POLE:
				switch (throwResult){
				case ThrowResult.DROP:
					if (isOnFire) {
						d = R.drawable.bxs_pole_fire;
					} else {
						d = R.drawable.bxs_pole_drop;
					}
					break;
				case ThrowResult.CATCH:
					d = R.drawable.bxs_pole_catch;
					break;
				case ThrowResult.STALWART:
					d = R.drawable.bxs_pole_stalwart;
					break;
				}
				break;
			case ThrowType.CUP:
				switch (throwResult){
				case ThrowResult.DROP:
					if (isOnFire) {
						d = R.drawable.bxs_cup_fire;
					} else {
						d = R.drawable.bxs_cup_drop;
					}
					break;
				case ThrowResult.CATCH:
					d = R.drawable.bxs_cup_catch;
					break;
				case ThrowResult.STALWART:
					d = R.drawable.bxs_cup_stalwart;
					break;
				}
				break;
			case ThrowType.BOTTLE:
				switch (throwResult){
					case ThrowResult.DROP:
						if (isOnFire) {
							d = R.drawable.bxs_bottle_fire;
						} else {
							d = R.drawable.bxs_bottle_drop;
						}
						break;
					case ThrowResult.CATCH:
						d = R.drawable.bxs_bottle_catch;
						break;
					case ThrowResult.STALWART:
						d = R.drawable.bxs_bottle_stalwart;
						break;
				}
				break;
		}
		return d;
	}
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getThrowIdx() {
		return throwNumber;
	}

	public void setThrowIdx(int throwNumber) {
		this.throwNumber = throwNumber;
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getThrowType() {
		return throwType;
	}

	public void setThrowType(int throwType) {
		this.throwType = throwType;
	}

	public int getThrowResult() {
		return throwResult;
	}

	public void setThrowResult(int throwResult) {
		this.throwResult = throwResult;
	}

	public int getErrorScore() {
		return errorScore;
	}

	public void setErrorScore(int errorScore) {
		this.errorScore = errorScore;
	}

	public int getOwnGoalScore() {
		return ownGoalScore;
	}

	public void setOwnGoalScore(int ownGoalScore) {
		this.ownGoalScore = ownGoalScore;
	}

	public int getGoaltendScore() {
		return goaltendScore;
	}

	public void setGoaltendScore(int goaltendScore) {
		this.goaltendScore = goaltendScore;
	}

	public int getInitialOffensivePlayerScore() {
		return initialOffensivePlayerScore;
	}

	public void setInitialOffensivePlayerScore(int initialOffensivePlayerScore) {
		this.initialOffensivePlayerScore = initialOffensivePlayerScore;
	}

	public int getInitialDefensivePlayerScore() {
		return initialDefensivePlayerScore;
	}

	public void setInitialDefensivePlayerScore(int initialDefensivePlayerScore) {
		this.initialDefensivePlayerScore = initialDefensivePlayerScore;
	}

	public int compareTo(Throw another) {
		if (throwNumber<another.throwNumber){
			return -1;
		}
		else if(throwNumber==another.throwNumber){
			return 0;
		}
		else{
			return 1;
		}
	}

	public static boolean isP1Throw(int throwNr) {
		return throwNr%2==0;
	}
	public static boolean isP1Throw(Throw t){
		return isP1Throw(t.getThrowIdx());
	}
	public boolean isP1Throw(){
		return isP1Throw(throwNumber);
	}
	

	
}
