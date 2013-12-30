package com.ultimatepolish.rulesets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;
import android.widget.Toast;

import com.ultimatepolish.db.Throw;
import com.ultimatepolish.enums.DeadType;
import com.ultimatepolish.enums.ThrowResult;
import com.ultimatepolish.enums.ThrowType;
import com.ultimatepolish.polishscorebook.R;

public class RuleSet00 implements RuleSet {
	/**
	 * Standard rules without coercion. TODO: remove fire rules, add in manual
	 * fire controls?
	 */

	public RuleSet00() {
	}

	public int getId() {
		return 0;
	}

	public String getDescription() {
		return "Standard ruleset with no coercion";
	}

	public boolean useAutoFire() {
		return false;
	}

	public void setThrowType(Throw t, int throwType) {
		t.throwType = throwType;
		if (throwType == ThrowType.FIRED_ON) {
			setThrowResult(t, ThrowResult.NA);
		}
	}

	public void setThrowResult(Throw t, int throwResult) {
		t.throwResult = throwResult;
	}

	public void setDeadType(Throw t, int deadType) {
		t.deadType = deadType;
	}

	public void setIsTipped(Throw t, boolean isTipped) {
		t.isTipped = isTipped;
	}

	public void setOwnGoals(Throw t, boolean[] ownGoals) {
		t.setOwnGoals(ownGoals);
	}

	public void setDefErrors(Throw t, boolean[] defErrors) {
		t.setDefErrors(defErrors);
	}

	public int[] getScoreDifferentials(Throw t) {
		int[] diffs = { 0, 0 };
		switch (t.throwResult) {
		case ThrowResult.NA:
			if (t.throwType == ThrowType.TRAP) {
				diffs[0] = -1;
			} else if (isOnFire(t)) {
				if (!t.isTipped) {
					switch (t.throwType) {
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
			if (!t.isLineFault) {
				switch (t.throwType) {
				case ThrowType.STRIKE:
					if (!isDropScoreBlocked(t) && t.deadType == DeadType.ALIVE) {
						diffs[0] = 1;
					}
					break;
				case ThrowType.POLE:
				case ThrowType.CUP:
					if (!t.isTipped) {
						diffs[0] = 2;
						if (t.isGoaltend) {
							// if goaltended, an extra point for dropping disc
							diffs[0] += 1;
						}
					}
					break;
				case ThrowType.BOTTLE:
					if (!t.isTipped) {
						diffs[0] = 3;
						if (t.isGoaltend) {
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
			if (!t.isLineFault) {
				switch (t.throwType) {
				case ThrowType.POLE:
				case ThrowType.CUP:
					if (!t.isTipped) {
						if (t.isGoaltend) {
							// if goaltended, award points for hit
							diffs[0] = 2;
						}
					}
					break;
				case ThrowType.BOTTLE:
					if (!t.isTipped) {
						if (t.isGoaltend) {
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
			if (isStackHit(t)) {
				diffs[1] = 1;
			}
			break;
		case ThrowResult.BROKEN:
			if (!t.isLineFault) {
				diffs[0] = 20;
			}
			break;
		default:
			break;
		}

		// extra points for other modifiers
		if (t.isDrinkHit) {
			diffs[1] -= 1;
		}
		if (t.isGrabbed) {
			diffs[0] += 1;
		}
		if (t.isOffensiveDrinkDropped) {
			diffs[0] -= 1;
		}
		if (t.isOffensivePoleKnocked) {
			diffs[1] += 2;
		}
		if (t.isOffensiveBottleKnocked) {
			diffs[1] += 3;
		}
		if (t.isOffensiveBreakError) {
			diffs[1] += 20;
		}
		if (t.isDefensiveDrinkDropped) {
			diffs[1] -= 1;
		}
		if (t.isDefensivePoleKnocked) {
			diffs[0] += 2;
		}
		if (t.isDefensiveBottleKnocked) {
			diffs[0] += 3;
		}
		if (t.isDefensiveBreakError) {
			diffs[0] += 20;
		}

		return diffs;
	}

	public int[] getFinalScores(Throw t) {
		int[] diff = getScoreDifferentials(t);
		int[] finalScores = { t.initialOffensivePlayerScore + diff[0],
				t.initialDefensivePlayerScore + diff[1] };
		return finalScores;
	}

	public String getSpecialString(Throw t) {
		String s = "";

		if (t.isLineFault) {
			s += "lf.";
		}

		if (t.isDrinkHit) {
			s += "d.";
		}

		if (t.isGoaltend) {
			s += "gt.";
		}

		if (t.isGrabbed) {
			s += "g.";
		}

		int og = 0;
		// technically drink drops are -1 for player instead of +1 for opponent,
		// but subtracting the value for display purposes would be more
		// confusing
		// this is really displaying the resulting differential due to og
		if (t.isOffensiveDrinkDropped) {
			og += 1;
		}
		if (t.isOffensivePoleKnocked) {
			og += 2;
		}
		if (t.isOffensiveBottleKnocked) {
			og += 3;
		}
		if (t.isOffensiveBreakError) {
			og += 20;
		}
		if (og > 0) {
			s += "og" + String.valueOf(og) + '.';
		}

		int err = 0;
		// same as for og
		if (t.isDefensiveDrinkDropped) {
			err += 1;
		}
		if (t.isDefensivePoleKnocked) {
			err += 2;
		}
		if (t.isDefensiveBottleKnocked) {
			err += 3;
		}
		if (t.isDefensiveBreakError) {
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

	public void setThrowDrawable(Throw t, ImageView iv) {
		List<Drawable> boxIconLayers = new ArrayList<Drawable>();

		if (!isValid(t, iv.getContext())) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_badthrow));
		}
		switch (t.throwType) {
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
			if (t.throwResult == ThrowResult.CATCH || isOnFire(t)) {
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

		switch (t.throwResult) {
		case ThrowResult.DROP:
			if (t.throwType != ThrowType.BALL_HIGH
					&& t.throwType != ThrowType.BALL_RIGHT
					&& t.throwType != ThrowType.BALL_LOW
					&& t.throwType != ThrowType.BALL_LEFT)
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

		switch (t.deadType) {
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

		if (isOnFire(t)) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_fire));
		}
		if (t.isTipped) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_tipped));
		}

		iv.setImageDrawable(new LayerDrawable(boxIconLayers
				.toArray(new Drawable[0])));
	}

	public boolean isDropScoreBlocked(Throw t) {
		boolean isBlocked = false;
		int oScore = t.initialOffensivePlayerScore;
		int dScore = t.initialDefensivePlayerScore;

		if (oScore < 10 && dScore < 10) {
			isBlocked = false;
		} else if (oScore >= 10 && dScore < oScore && dScore < 10) {
			isBlocked = true;
		} else if (oScore >= 10 && dScore >= 10 && oScore > dScore) {
			isBlocked = true;
		}

		return isBlocked;
	}

	public boolean isFiredOn(Throw t) {
		if (t.defenseFireCount >= 3) {
			assert t.offenseFireCount < 3 : "both players cant be on fire";
			return true;
		} else {
			return false;
		}
	}

	public boolean isOnFire(Throw t) {
		if (t.offenseFireCount >= 3) {
			assert t.defenseFireCount < 3 : "both players cant be on fire";
			return true;
		} else {
			return false;
		}
	}

	public void setFireCounts(Throw t, Throw previousThrow) {
	}

	public boolean isValid(Throw t, Context context) {
		boolean valid = isValid(t);
		if (!valid) {
			Toast.makeText(context, t.invalidMessage, Toast.LENGTH_LONG).show();
		}
		return valid;
	}

	public boolean isValid(Throw t) {
		boolean valid = true;
		t.invalidMessage = "(gameId=%d, throwIdx=%d) ";
		t.invalidMessage = String.format(t.invalidMessage, t.getGame().getId(),
				t.throwIdx);
		if (isOnFire(t)) {
			if (t.throwResult != ThrowResult.NA
					&& t.throwResult != ThrowResult.BROKEN) {
				valid = false;
				t.invalidMessage += "OnFire => NA or Broken. ";
			}
		}
		if (isFiredOn(t)) {
			if (t.throwType != ThrowType.FIRED_ON
					&& t.throwType != ThrowType.NOT_THROWN) {
				valid = false;
				t.invalidMessage += "ThrowType should be Fired_on. ";
			}
		}
		switch (t.throwType) {
		case ThrowType.BALL_HIGH:
		case ThrowType.BALL_RIGHT:
		case ThrowType.BALL_LOW:
		case ThrowType.BALL_LEFT:
		case ThrowType.STRIKE:
			if (t.deadType != DeadType.ALIVE && t.isDrinkHit) {
				valid = false;
				t.invalidMessage += "Drink hit => Alive";
			} else if (t.isGoaltend) {
				valid = false;
				t.invalidMessage += "SHRLL != Goaltend. ";
			} else if (t.isTipped) {
				valid = false;
				t.invalidMessage += "SHRLL != Tipped. ";
			}

			switch (t.throwResult) {
			case ThrowResult.DROP:
			case ThrowResult.CATCH:
				break;
			default:
				if (!isOnFire(t)) {
					valid = false;
					t.invalidMessage += "SHRLL => Drop or catch. ";
				}
				break;
			}
			break;

		case ThrowType.POLE:
		case ThrowType.CUP:
		case ThrowType.BOTTLE:
			if (t.isGrabbed) {
				valid = false;
				t.invalidMessage += "Grabbed + PCB should be Goaltend. ";
			}
			if (t.isDrinkHit) {
				valid = false;
				t.invalidMessage += "PCB != Drink hit. ";
			}
			if (t.isTipped && t.isGoaltend) {
				valid = false;
				t.invalidMessage += "PCB != Tipped + goaltend simultaneously. ";
			}
			if (t.deadType != DeadType.ALIVE && t.isGoaltend) {
				valid = false;
				t.invalidMessage += "Dead != Goaltend. ";
			}
			if (t.throwResult == ThrowResult.NA && !isOnFire(t)) {
				valid = false;
				t.invalidMessage += "PCB w/o Fire != NA result. ";
			}
			if (t.isTipped && t.throwResult == ThrowResult.STALWART) {
				valid = false;
				t.invalidMessage += "Stalwart != Tipped. ";
			}
			if (t.isGoaltend && t.throwResult == ThrowResult.STALWART) {
				valid = false;
				t.invalidMessage += "Stalwart != Goaltend. ";
			}
			if (t.isTipped && t.throwResult == ThrowResult.BROKEN) {
				valid = false;
				t.invalidMessage += "Tipped != Broken. ";
			}
			if (t.isGoaltend && t.throwResult == ThrowResult.BROKEN) {
				valid = false;
				t.invalidMessage += "Goaltend != Broken. ";
			}
			break;

		case ThrowType.TRAP:
		case ThrowType.SHORT:
			if (t.isGoaltend || t.isTipped || t.isDrinkHit) {
				valid = false;
				t.invalidMessage += "Trap / short != Goaltend, tip or drinkHit. ";
			} else if (t.throwResult != ThrowResult.NA) {
				valid = false;
				t.invalidMessage += "Trap / short => NA result. ";
			} else if (t.deadType == DeadType.ALIVE) {
				valid = false;
				t.invalidMessage += "Trap / short != Alive. ";
			}
			break;
		case ThrowType.TRAP_REDEEMED:
			if (t.isGoaltend || t.isTipped || t.isDrinkHit) {
				valid = false;
				t.invalidMessage += "Trap != Goaltend, tip or drinkHit. ";
			} else if (t.throwResult != ThrowResult.NA
					&& t.throwResult != ThrowResult.BROKEN) {
				valid = false;
				t.invalidMessage += "Redeemed Trap => Broken or NA result. ";
			} else if (t.deadType == DeadType.ALIVE) {
				valid = false;
				t.invalidMessage += "Trap != Alive. ";
			}
			break;

		case ThrowType.FIRED_ON:
			// fired_on is a dummy throw, so modifiers dont count and result
			// must be NA
			// errors could potentially happen while returning the disc, so
			// those are allowed
			if (t.defenseFireCount < 3) {
				valid = false;
				t.invalidMessage += "Fired-on but opponent not on fire. ";
			} else if (t.isLineFault || t.isGoaltend || t.isTipped
					|| t.isDrinkHit || t.deadType != DeadType.ALIVE) {
				valid = false;
				t.invalidMessage += "Fired-on != any modifier. ";
			} else if (t.throwResult != ThrowResult.NA) {
				valid = false;
				t.invalidMessage += "Fired-on => NA result. ";
			}

			break;
		}
		// logd("isValid",invalidMessage);
		return valid;
	}

	public boolean isStackHit(Throw t) {
		return t.isStackHit();
	}

	public boolean isOffensiveError(Throw t) {
		return (t.isOffensiveBottleKnocked || t.isOffensivePoleKnocked
				|| t.isOffensivePoleKnocked || t.isOffensiveBreakError
				|| t.isOffensiveDrinkDropped || t.isLineFault);
	}

	public boolean isDefensiveError(Throw t) {
		return (t.isDefensiveBottleKnocked || t.isDefensivePoleKnocked
				|| t.isDefensivePoleKnocked || t.isDefensiveBreakError
				|| t.isDefensiveDrinkDropped || t.isDrinkHit);
	}
}