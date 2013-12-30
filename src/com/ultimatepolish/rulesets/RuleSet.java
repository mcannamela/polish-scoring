package com.ultimatepolish.rulesets;

import android.content.Context;
import android.widget.ImageView;

import com.ultimatepolish.db.Throw;

public interface RuleSet {
	public int getId();

	public String getDescription();

	public boolean useAutoFire();

	// Primary setting functions ==============================================
	public void setThrowType(Throw t, int throwType);

	public void setThrowResult(Throw t, int throwResult);

	public void setDeadType(Throw t, int deadType);

	public void setIsTipped(Throw t, boolean isTipped);

	public void setOwnGoals(Throw t, boolean[] ownGoals);

	public void setDefErrors(Throw t, boolean[] defErrors);

	// Scores and UI ==========================================================
	public int[] getScoreDifferentials(Throw t);

	public int[] getFinalScores(Throw t);

	public String getSpecialString(Throw t);

	public void setThrowDrawable(Throw t, ImageView iv);

	// Special Rules ==========================================================
	public boolean isDropScoreBlocked(Throw t);

	public boolean isFiredOn(Throw t);

	public boolean isOnFire(Throw t);

	public void setFireCounts(Throw t, Throw previousThrow);

	// Validation =============================================================
	public boolean isValid(Throw t, Context context);

	public boolean isValid(Throw t);

	// Convenience Definitions ================================================
	public boolean isStackHit(Throw t);

	public boolean isOffensiveError(Throw t);

	public boolean isDefensiveError(Throw t);
}