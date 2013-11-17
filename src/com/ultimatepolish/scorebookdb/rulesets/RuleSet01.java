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
		if (t.defenseFireCount >= 3) {
			t.throwType = ThrowType.FIRED_ON;
			setDeadType(t, DeadType.ALIVE);
		} else {
			t.throwType = throwType;
		}

		switch (throwType) {
		case ThrowType.BALL_HIGH:
		case ThrowType.BALL_RIGHT:
		case ThrowType.BALL_LOW:
		case ThrowType.BALL_LEFT:
		case ThrowType.STRIKE:
			if (t.throwResult != ThrowResult.DROP
					&& t.throwResult != ThrowResult.CATCH) {
				t.throwResult = ThrowResult.CATCH;
			}
			break;
		case ThrowType.SHORT:
		case ThrowType.TRAP:
		case ThrowType.TRAP_REDEEMED:
		case ThrowType.FIRED_ON:
		case ThrowType.NOT_THROWN:
			// TODO: what if redeemed trap breaks bottle?
			setThrowResult(t, ThrowResult.NA);
			break;
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