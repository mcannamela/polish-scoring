package com.ultimatepolish.scorebookdb.rulesets;

import android.util.Log;

import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.enums.DeadType;
import com.ultimatepolish.scorebookdb.enums.ThrowResult;
import com.ultimatepolish.scorebookdb.enums.ThrowType;

public class RuleSet01 extends RuleSet00 {
	/**
	 * Standard rules with coercion and autofire.
	 */

	public RuleSet01() {
	}

	@Override
	public void setThrowType(Throw t, int throwType) {
		if (t.offenseFireCount >= 3) {
			t.throwType = ThrowType.FIRED_ON;
		} else {
			t.throwType = throwType;
			if (throwType == ThrowType.SHORT || throwType == ThrowType.TRAP
					|| throwType == ThrowType.TRAP_REDEEMED) {
				t.throwResult = ThrowResult.NA;
			}
		}
	}

	@Override
	public void setThrowResult(Throw t, int throwResult) {

		// TODO: this block should probably be on UI side
		switch (throwResult) {
		case ThrowResult.BROKEN:
			if (throwResult == ThrowResult.BROKEN
					&& t.throwResult == ThrowResult.BROKEN) {
				t.throwResult = ThrowResult.CATCH;
			} else {
				t.throwResult = ThrowResult.BROKEN;
			}
			break;
		}

		if (t.offenseFireCount >= 3) {
			t.throwResult = ThrowResult.NA;
		} else {
			// some error checking
			switch (t.throwType) {
			case ThrowType.BALL_HIGH:
			case ThrowType.BALL_RIGHT:
			case ThrowType.BALL_LOW:
			case ThrowType.BALL_LEFT:
			case ThrowType.STRIKE:
				if (throwResult != ThrowResult.DROP
						&& throwResult != ThrowResult.CATCH) {
					t.throwResult = ThrowResult.CATCH;
				} else {
					t.throwResult = throwResult;
				}
				break;
			case ThrowType.TRAP:
			case ThrowType.TRAP_REDEEMED:
			case ThrowType.SHORT:
			case ThrowType.FIRED_ON:
				throwResult = ThrowResult.NA;
				break;
			default:
				break;
			}
			if (t.defenseFireCount >= 3) {
				throwResult = ThrowResult.NA;
			}

			if (throwResult == ThrowResult.BROKEN) {
				t.throwResult = ThrowResult.BROKEN;
			} else if (throwResult == ThrowResult.NA) {
				t.throwResult = ThrowResult.NA;
			}
		}
	}

	@Override
	public void setDeadType(Throw t, int deadType) {
		t.deadType = deadType;
	}

	@Override
	public void setOwnGoals(Throw t, boolean[] ownGoals) {
		if (t.offenseFireCount >= 3) {
			t.isTipped = false;
			t.deadType = DeadType.ALIVE;
			t.isLineFault = false;
		} else {
			t.setOwnGoals(ownGoals);
		}
	}

	@Override
	public void setDefErrors(Throw t, boolean[] defErrors) {
		if (t.offenseFireCount >= 3) {
			t.isTipped = false;
			t.deadType = DeadType.ALIVE;
			t.isGoaltend = false;
			t.isDrinkHit = false;
		} else {
			t.setDefErrors(defErrors);
		}
	}

	@Override
	public boolean stokesOffensiveFire(Throw t) {
		// you didn't quench yourself, hit the stack, your opponent didn't
		// stalwart
		boolean stokes = (!quenchesOffensiveFire(t) && isStackHit(t) && !(t.throwResult == ThrowResult.STALWART));
		return stokes;
	}

	@Override
	public boolean quenchesOffensiveFire(Throw t) {
		boolean quenches = isOffensiveError(t)
				|| (t.deadType != DeadType.ALIVE);
		return quenches;
	}

	@Override
	public boolean quenchesDefensiveFire(Throw t) {
		// offense hit the stack and defense failed to defend, or offense was on
		// fire

		boolean defenseFailed = (t.throwResult == ThrowResult.DROP)
				|| (t.throwResult == ThrowResult.BROKEN)
				|| (isOnFire(t) && !t.isTipped);

		boolean quenches = isStackHit(t) && defenseFailed;

		// defensive error will also quench
		quenches = quenches || isDefensiveError(t);

		return quenches;
	}

	@Override
	public void setFireCounts(Throw t, Throw previousThrow) {
		int oldOffenseCount = previousThrow.defenseFireCount;
		int oldDefenseCount = previousThrow.offenseFireCount;
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
			} else if (stokesOffensiveFire(t)) {
				newOffenseCount++;
			} else {
				newOffenseCount = 0;
			}
			if (quenchesDefensiveFire(t)) {
				newDefenseCount = 0;
			}
		}

		t.offenseFireCount = newOffenseCount;
		t.defenseFireCount = newDefenseCount;

		Log.i("Throw.setFireCounts()", "o=" + newOffenseCount + ", d="
				+ newDefenseCount);
	}
}