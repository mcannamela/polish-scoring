package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.ultimatepolish.polishscorebook.R;

public class Throw implements Comparable<Throw> {
	public static final String THROW_INDEX = "throwIdx";
	public static final String GAME_ID = "game_id";
	public static final String OFFENSIVE_PLAYER = "offensivePlayer_id";
	public static final String DEFENSIVE_PLAYER = "defensivePlayer_id";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(canBeNull = false, uniqueCombo = true)
	private int throwIdx;

	@DatabaseField(canBeNull = false, uniqueCombo = true, foreign = true)
	private Game game;

	@DatabaseField(canBeNull = false, foreign = true)
	private Player offensivePlayer;

	@DatabaseField(canBeNull = false, foreign = true)
	private Player defensivePlayer;

	@DatabaseField(canBeNull = false)
	private Date timestamp;

	@DatabaseField(canBeNull = false)
	private int throwType;

	@DatabaseField(canBeNull = false)
	private int throwResult;

	@DatabaseField
	private int deadType = DeadType.ALIVE;

	@DatabaseField
	public boolean isTipped = false;

	@DatabaseField
	public boolean isGoaltend = false;

	@DatabaseField
	public boolean isGrabbed = false;

	@DatabaseField
	public boolean isDrinkHit = false;

	@DatabaseField
	public boolean isLineFault = false;

	@DatabaseField
	public boolean isOffensiveDrinkDropped = false;

	@DatabaseField
	public boolean isOffensivePoleKnocked = false;

	@DatabaseField
	public boolean isOffensiveBottleKnocked = false;

	@DatabaseField
	public boolean isOffensiveBreakError = false;

	@DatabaseField
	public boolean isDefensiveDrinkDropped = false;

	@DatabaseField
	public boolean isDefensivePoleKnocked = false;

	@DatabaseField
	public boolean isDefensiveBottleKnocked = false;

	@DatabaseField
	public boolean isDefensiveBreakError = false;

	@DatabaseField
	private int offenseFireCount = 0;

	@DatabaseField
	private int defenseFireCount = 0;

	@DatabaseField
	private int initialOffensivePlayerScore = 0;

	@DatabaseField
	private int initialDefensivePlayerScore = 0;

	private String invalidMessage = "";

	Throw() {

	}

	public Throw(int throwIdx, Game game, Player offensivePlayer,
			Player defensivePlayer, Date timestamp, int throwType,
			int throwResult) {
		super();
		this.throwIdx = throwIdx;
		this.game = game;
		this.offensivePlayer = offensivePlayer;
		this.defensivePlayer = defensivePlayer;
		this.timestamp = timestamp;
		this.throwType = throwType;
		this.throwResult = throwResult;
	}

	public Throw(int throwIdx, Game game, Player offensivePlayer,
			Player defensivePlayer, Date timestamp) {
		super();
		this.throwIdx = throwIdx;
		this.game = game;
		this.offensivePlayer = offensivePlayer;
		this.defensivePlayer = defensivePlayer;
		this.timestamp = timestamp;
		this.throwType = ThrowType.NOT_THROWN;
	}

	public static Dao<Throw, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Throw, Long> d = null;
		try {
			d = helper.getThrowDao();
		} catch (SQLException e) {
			throw new RuntimeException("Couldn't get dao: ", e);
		}
		return d;
	}

	public HashMap<String, Object> getQueryMap() {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put(Throw.THROW_INDEX, getThrowIdx());
		m.put(Throw.GAME_ID, getGame());
		return m;
	}

	public void setFireCounts(Throw previousThrow) {
		int oldOffenseCount = previousThrow.getDefenseFireCount();
		int oldDefenseCount = previousThrow.getOffenseFireCount();
		int newOffenseCount = oldOffenseCount;
		int newDefenseCount = oldDefenseCount;

		// previous throw, opponent was or went on fire
		if (oldDefenseCount >= 3) {
			newOffenseCount = oldOffenseCount;
			newDefenseCount = oldDefenseCount;
		}
		// opponent not on fire last throw so we have a chance to change things
		else {
			if (oldOffenseCount == 3) {
				newOffenseCount++;
			} else if (stokesOffensiveFire()) {
				newOffenseCount++;
			} else {
				newOffenseCount = 0;
			}
			if (quenchesDefensiveFire()) {
				newDefenseCount = 0;
			}
		}

		setOffenseFireCount(newOffenseCount);
		setDefenseFireCount(newDefenseCount);

		Log.i("Throw.setFireCounts()", "o=" + newOffenseCount + ", d="
				+ newDefenseCount);
	}

	public void setInitialScores(Throw previousThrow) {
		int[] scores = previousThrow.getFinalScores();
		setInitialDefensivePlayerScore(scores[0]);
		setInitialOffensivePlayerScore(scores[1]);
	}

	public void setInitialScores() {
		setInitialDefensivePlayerScore(0);
		setInitialOffensivePlayerScore(0);
	}

	public int[] getFinalScores() {
		int[] diff = getScoreDifferentials();
		int[] finalScores = { initialOffensivePlayerScore + diff[0],
				initialDefensivePlayerScore + diff[1] };
		return finalScores;
	}

	private int[] getScoreDifferentials() {
		int[] diffs = { 0, 0 };
		switch (throwResult) {
		case ThrowResult.NA:
			if (throwType == ThrowType.TRAP) {
				diffs[0] = -1;
			} else if (isOnFire()) {
				if (!isTipped) {
					switch (throwType) {
					case ThrowType.BOTTLE:
						diffs[0] = 3;
						break;
					case ThrowType.CUP:
					case ThrowType.POLE:
						diffs[0] = 2;
						break;
					}
				}
			}
			break;
		case ThrowResult.DROP:
			if (!isLineFault) {
				switch (throwType) {
				case ThrowType.STRIKE:
					if (!isDropScoreBlocked() && deadType == DeadType.ALIVE) {
						diffs[0] = 1;
					}
					break;
				case ThrowType.POLE:
				case ThrowType.CUP:
					if (!isTipped) {
						diffs[0] = 2;
						if (isGoaltend) {
							// if goaltended, an extra point for dropping disc
							diffs[0] += 1;
						}
					}
					break;
				case ThrowType.BOTTLE:
					if (!isTipped) {
						diffs[0] = 3;
						if (isGoaltend) {
							// if goaltended, an extra point for dropping disc
							diffs[0] += 1;
						}
					}
					break;
				default:
					break;
				}
			}
			break;
		case ThrowResult.CATCH:
			if (!isLineFault) {
				switch (throwType) {
				case ThrowType.POLE:
				case ThrowType.CUP:
					if (!isTipped) {
						if (isGoaltend) {
							// if goaltended, award points for hit
							diffs[0] = 2;
						}
					}
					break;
				case ThrowType.BOTTLE:
					if (!isTipped) {
						if (isGoaltend) {
							// if goaltended, award points for hit
							diffs[0] = 3;
						}
					}
					break;
				default:
					break;
				}
			}
			break;
		case ThrowResult.STALWART:
			if (isStackHit()) {
				diffs[1] = 1;
			}
			break;
		case ThrowResult.BROKEN:
			if (!isLineFault) {
				diffs[0] = 20;
			}
			break;
		default:
			break;
		}

		// extra points for other modifiers
		if (isDrinkHit) {
			diffs[1] -= 1;
		}
		if (isGrabbed) {
			diffs[0] += 1;
		}
		if (isOffensiveDrinkDropped) {
			diffs[0] -= 1;
		}
		if (isOffensivePoleKnocked) {
			diffs[1] += 2;
		}
		if (isOffensiveBottleKnocked) {
			diffs[1] += 3;
		}
		if (isOffensiveBreakError) {
			diffs[1] += 20;
		}
		if (isDefensiveDrinkDropped) {
			diffs[1] -= 1;
		}
		if (isDefensivePoleKnocked) {
			diffs[0] += 2;
		}
		if (isDefensiveBottleKnocked) {
			diffs[0] += 3;
		}
		if (isDefensiveBreakError) {
			diffs[0] += 20;
		}

		return diffs;
	}

	private boolean isDropScoreBlocked() {
		boolean isBlocked = false;
		int oScore = initialOffensivePlayerScore;
		int dScore = initialDefensivePlayerScore;

		if (oScore < 10 && dScore < 10) {
			isBlocked = false;
		} else if (oScore >= 10 && dScore < oScore && dScore < 10) {
			isBlocked = true;
		} else if (oScore >= 10 && dScore >= 10 && oScore > dScore) {
			isBlocked = true;
		}

		return isBlocked;
	}

	public boolean isOffensiveError() {
		return (isOffensiveBottleKnocked || isOffensivePoleKnocked
				|| isOffensivePoleKnocked || isOffensiveBreakError
				|| isOffensiveDrinkDropped || isLineFault);
	}

	public boolean isDefensiveError() {
		return (isDefensiveBottleKnocked || isDefensivePoleKnocked
				|| isDefensivePoleKnocked || isDefensiveBreakError
				|| isDefensiveDrinkDropped || isDrinkHit);
	}

	public boolean isStackHit() {
		return (throwType == ThrowType.POLE || throwType == ThrowType.CUP || throwType == ThrowType.BOTTLE);
	}

	public boolean isOnFire() {
		if (offenseFireCount > 3) {
			assert defenseFireCount < 3 : "should not be possible to have both players with fire counts >=3";
			return true;
		} else {
			return false;
		}
	}

	public boolean isFiredOn() {
		if (defenseFireCount >= 3) {
			assert offenseFireCount < 3 : "should not be possible to have both players with fire counts >=3";
			return true;
		} else {
			return false;
		}
	}

	public boolean stokesOffensiveFire() {
		// you didn't quench yourself, hit the stack, your opponent didn't
		// stalwart
		boolean stokes = (!quenchesOffensiveFire() && isStackHit() && !(throwResult == ThrowResult.STALWART));
		return stokes;
	}

	public boolean quenchesOffensiveFire() {
		boolean quenches = isOffensiveError() || (deadType != DeadType.ALIVE);
		return quenches;
	}

	public boolean quenchesDefensiveFire() {
		// offense hit the stack and defense failed to defend, or offense was on
		// fire

		boolean defenseFailed = (throwResult == ThrowResult.DROP)
				|| (throwResult == ThrowResult.BROKEN)
				|| (isOnFire() && !isTipped);

		boolean quenches = isStackHit() && defenseFailed;

		// defensive error will also quench
		quenches = quenches || isDefensiveError();

		return quenches;
	}

	public String getSpecialString() {
		String s = "";

		if (isLineFault) {
			s += "lf.";
		}

		if (isDrinkHit) {
			s += "d.";
		}

		if (isGoaltend) {
			s += "gt.";
		}

		if (isGrabbed) {
			s += "g.";
		}

		int og = 0;
		// technically drink drops are -1 for player instead of +1 for opponent,
		// but subtracting the value for display purposes would be more
		// confusing
		// this is really displaying the resulting differential due to og
		if (isOffensiveDrinkDropped) {
			og += 1;
		}
		if (isOffensivePoleKnocked) {
			og += 2;
		}
		if (isOffensiveBottleKnocked) {
			og += 3;
		}
		if (isOffensiveBreakError) {
			og += 20;
		}
		if (og > 0) {
			s += "og" + String.valueOf(og) + '.';
		}

		int err = 0;
		// same as for og
		if (isDefensiveDrinkDropped) {
			err += 1;
		}
		if (isDefensivePoleKnocked) {
			err += 2;
		}
		if (isDefensiveBottleKnocked) {
			err += 3;
		}
		if (isDefensiveBreakError) {
			err += 20;
		}
		if (err > 0) {
			s += "e" + String.valueOf(err) + '.';
		}

		if (s.length() == 0) {
			s = "--";
		} else {
			// pop the last '.' off the end of the string
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	public void setThrowDrawable(ImageView iv) {
		List<Drawable> boxIconLayers = new ArrayList<Drawable>();

		if (!isValid(iv.getContext())) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_badthrow));
		}
		switch (throwType) {
		case ThrowType.BOTTLE:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_bottle));
			break;
		case ThrowType.CUP:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_cup));
			break;
		case ThrowType.POLE:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_pole));
			break;
		case ThrowType.STRIKE:
			if (throwResult == ThrowResult.CATCH || isOnFire()) {
				boxIconLayers.add(iv.getResources().getDrawable(
						R.drawable.bxs_under_strike));
			}
			break;
		case ThrowType.BALL_HIGH:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_high));
			break;
		case ThrowType.BALL_RIGHT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_right));
			break;
		case ThrowType.BALL_LOW:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_low));
			break;
		case ThrowType.BALL_LEFT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_left));
			break;
		case ThrowType.SHORT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_short));
			break;
		case ThrowType.TRAP:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_trap));
			break;
		case ThrowType.TRAP_REDEEMED:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_trap));
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_drop));
			break;
		case ThrowType.NOT_THROWN:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_notthrown));
			break;
		case ThrowType.FIRED_ON:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_firedon));
			break;
		default:
			boxIconLayers.add(iv.getResources()
					.getDrawable(R.drawable.bxs_oops));
			break;
		}

		switch (throwResult) {
		case ThrowResult.DROP:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_drop));
			break;
		case ThrowResult.STALWART:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_stalwart));
			break;
		case ThrowResult.BROKEN:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_break));
			break;
		}

		switch (deadType) {
		case DeadType.HIGH:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_high));
			break;
		case DeadType.RIGHT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_right));
			break;
		case DeadType.LOW:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_low));
			break;
		case DeadType.LEFT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_left));
			break;
		}

		if (isOnFire()) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_fire));
		}
		if (isTipped) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_tipped));
		}

		iv.setImageDrawable(new LayerDrawable(boxIconLayers
				.toArray(new Drawable[0])));
	}

	public String getInvalidMessage() {
		return invalidMessage;
	}

	public boolean isValid(Context context) {
		boolean valid = isValid();
		if (!valid) {
			Toast.makeText(context, invalidMessage, Toast.LENGTH_LONG).show();
		}
		return valid;
	}

	public boolean isValid() {
		boolean valid = true;
		invalidMessage = "(gameId=%d, throwIdx=%d)";
		invalidMessage = String.format(invalidMessage, game.getId(), throwIdx);
		if (isOnFire()) {
			if (throwResult != ThrowResult.NA
					&& throwResult != ThrowResult.BROKEN) {
				valid = false;
				invalidMessage += "OnFire => ThrowResult == NA or Broken. ";
			}
		}
		switch (throwType) {
		case ThrowType.BALL_HIGH:
		case ThrowType.BALL_RIGHT:
		case ThrowType.BALL_LOW:
		case ThrowType.BALL_LEFT:
		case ThrowType.STRIKE:
			if (deadType != DeadType.ALIVE && isDrinkHit) {
				valid = false;
				invalidMessage += "drinkHit => live throw";
			} else if (isGoaltend || isTipped) {
				valid = false;
				invalidMessage += "Goaltending || tipped => not SHRLL. ";
			}

			switch (throwResult) {
			case ThrowResult.DROP:
			case ThrowResult.CATCH:
				break;
			default:
				if (!isOnFire()) {
					valid = false;
					invalidMessage += "SHRLL => drop or catch. ";
				}
				break;
			}

			break;
		case ThrowType.POLE:
		case ThrowType.CUP:
		case ThrowType.BOTTLE:
			if (isGrabbed) {
				valid = false;
				invalidMessage += "grabbing a PCB hit should be marked goaltending. ";
			}
			if (isDrinkHit) {
				valid = false;
				invalidMessage += "drink hit <=>  not PCB hit. ";
			}
			if (isTipped && isGoaltend) {
				valid = false;
				invalidMessage += "PCB throws cant be tipped and goaltended simultaneously. ";
			}
			if (deadType != DeadType.ALIVE && isGoaltend) {
				valid = false;
				invalidMessage += "Dead <=> not goaltended. ";
			}
			if (throwResult == ThrowResult.NA && !isOnFire()) {
				valid = false;
				invalidMessage += "PCB and not onFire => not NA result. ";
			}
			if (isTipped && throwResult == ThrowResult.STALWART) {
				valid = false;
				invalidMessage += "stalwart <=> not tip. ";
			}
			if (isGoaltend && throwResult == ThrowResult.STALWART) {
				valid = false;
				invalidMessage += "stalwart <=> not goaltend. ";
			}
			if (isTipped && throwResult == ThrowResult.BROKEN) {
				valid = false;
				invalidMessage += "tip <=> not broken. ";
			}
			if (isGoaltend && throwResult == ThrowResult.BROKEN) {
				valid = false;
				invalidMessage += "goaltend <=> not broken. ";
			}

			break;

		case ThrowType.TRAP:
		case ThrowType.TRAP_REDEEMED:
		case ThrowType.SHORT:
			if (isGoaltend || isTipped || isDrinkHit) {
				valid = false;
				invalidMessage += "Goaltend or tip or drinkHit => not trap and not short. ";
			} else if (throwResult != ThrowResult.NA) {
				valid = false;
				invalidMessage += "Trap or short => NA result. ";
			}

			break;
		case ThrowType.FIRED_ON:
			// fired_on is a dummy throw, so modifiers dont count and result
			// must be NA
			// errors could potentially happen while returning the disc, so
			// those are allowed
			if (isLineFault || isGoaltend || isTipped || isDrinkHit
					|| deadType != DeadType.ALIVE) {
				valid = false;
				invalidMessage += "Fired-on cannot be modified. ";
			} else if (throwResult != ThrowResult.NA) {
				valid = false;
				invalidMessage += "Fired-on => NA result.";
			}

			break;
		}
		// logd("isValid",invalidMessage);
		return valid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getThrowIdx() {
		return throwIdx;
	}

	public void setThrowIdx(int throwIdx) {
		this.throwIdx = throwIdx;
	}

	public Game getGame() {
		return game;
	}

	public Player getOffensivePlayer() {
		return offensivePlayer;
	}

	public Player getDefensivePlayer() {
		return defensivePlayer;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getThrowType() {
		return throwType;
	}

	public void setThrowType(int throwType) {
		if (offenseFireCount >= 3) {
			this.throwType = ThrowType.FIRED_ON;
		} else {
			this.throwType = throwType;
		}
	}

	public int getThrowResult() {
		return throwResult;
	}

	public void setThrowResult(int throwResult) {
		if (offenseFireCount >= 3) {
			this.throwResult = ThrowResult.NA;
			// naView.setBackgroundColor(Color.RED);
		} else {
			// some error checking
			switch (throwType) {
			case ThrowType.BALL_HIGH:
			case ThrowType.BALL_RIGHT:
			case ThrowType.BALL_LOW:
			case ThrowType.BALL_LEFT:
			case ThrowType.STRIKE:
				if (throwResult != ThrowResult.DROP
						&& throwResult != ThrowResult.CATCH) {
					throwResult = ThrowResult.CATCH;
					// setThrowResultToNP(ThrowResult.CATCH);
				}
				break;
			case ThrowType.TRAP:
			case ThrowType.TRAP_REDEEMED:
			case ThrowType.SHORT:
			case ThrowType.FIRED_ON:
				throwResult = ThrowResult.NA;
				// naView.setBackgroundColor(Color.RED);
				break;
			default:
				break;
			}
			if (defenseFireCount >= 3) {
				throwResult = ThrowResult.NA;
				// naView.setBackgroundColor(Color.RED);
			}

			if (throwResult == ThrowResult.BROKEN) {
				this.throwResult = ThrowResult.BROKEN;
			} else if (throwResult == ThrowResult.NA) {
				this.throwResult = ThrowResult.NA;
				// naView.setBackgroundColor(Color.RED);
			}
		}
	}

	public int getDeadType() {
		return deadType;
	}

	public void setDeadType(int deadType) {
		this.deadType = deadType;
	}

	public int[] getFireCounts() {
		int[] fireCounts = { offenseFireCount, defenseFireCount };
		return fireCounts;
	}

	public void setFireCounts(int[] fireCounts) {
		this.offenseFireCount = fireCounts[0];
		this.defenseFireCount = fireCounts[1];
	}

	public int getOffenseFireCount() {
		return offenseFireCount;
	}

	public void setOffenseFireCount(int offenseFireCount) {
		this.offenseFireCount = offenseFireCount;
	}

	public int getDefenseFireCount() {
		return defenseFireCount;
	}

	public void setDefenseFireCount(int defenseFireCount) {
		this.defenseFireCount = defenseFireCount;
	}

	public int getInitialOffensivePlayerScore() {
		return initialOffensivePlayerScore;
	}

	public boolean[] getOwnGoals() {
		boolean[] ownGoals = { isLineFault, isOffensiveDrinkDropped,
				isOffensivePoleKnocked, isOffensiveBottleKnocked,
				isOffensiveBreakError };
		return ownGoals;
	}

	public void setOwnGoals(boolean[] ownGoals) {
		if (offenseFireCount >= 3) {
			isTipped = false;
			setDeadType(DeadType.ALIVE);
			isLineFault = false;
			isGoaltend = false;
			isDrinkHit = false;
		} else {
			isTipped = currentIsTipped;
			t.setDeadType(currentDeadType);
			for (View vw : deadViews) {
				vw.setBackgroundColor(Color.LTGRAY);
			}
			if (currentDeadType > 0) {
				deadViews[currentDeadType - 1].setBackgroundColor(Color.RED);
			}
			isLineFault = ownGoals[0];
			isOffensiveDrinkDropped = ownGoals[1];
			isOffensivePoleKnocked = ownGoals[2];
			isOffensiveBottleKnocked = ownGoals[3];
			isOffensiveBreakError = ownGoals[4];
		}

	}

	public boolean[] getDefErrors() {
		boolean[] defErrors = { isGoaltend, isGrabbed, isDrinkHit,
				isDefensiveDrinkDropped, isDefensivePoleKnocked,
				isDefensiveBottleKnocked, isDefensiveBreakError };
		return defErrors;
	}

	public void setDefErrors(boolean[] defErrors) {
		if (offenseFireCount >= 3) {
			isTipped = false;
			setDeadType(DeadType.ALIVE);
			isLineFault = false;
			isGoaltend = false;
			isDrinkHit = false;
		} else {
			isTipped = currentIsTipped;
			t.setDeadType(currentDeadType);
			for (View vw : deadViews) {
				vw.setBackgroundColor(Color.LTGRAY);
			}
			if (currentDeadType > 0) {
				deadViews[currentDeadType - 1].setBackgroundColor(Color.RED);
			}
			isGoaltend = defErrors[0];
			isGrabbed = defErrors[1];
			isDrinkHit = defErrors[2];
			isDefensiveDrinkDropped = defErrors[3];
			isDefensivePoleKnocked = defErrors[4];
			isDefensiveBottleKnocked = defErrors[5];
			isDefensiveBreakError = defErrors[6];
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
		if (throwIdx < another.throwIdx) {
			return -1;
		} else if (throwIdx == another.throwIdx) {
			return 0;
		} else {
			return 1;
		}
	}

	public static boolean isP1Throw(int throwIdx) {
		return throwIdx % 2 == 0;
	}

	public static boolean isP1Throw(Throw t) {
		return isP1Throw(t.getThrowIdx());
	}

	public boolean isP1Throw() {
		return isP1Throw(throwIdx);
	}

	private void logd(String method, String msg) {
		Log.d("Throw" + "." + method, msg);
	}

}