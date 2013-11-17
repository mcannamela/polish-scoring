package com.ultimatepolish.scorebookdb.rulesets;

import android.content.Context;
import android.widget.ImageView;

import com.ultimatepolish.scorebookdb.Throw;

public interface RuleSet {

	public void setThrowType(Throw t, int throwType);

	public void setThrowResult(Throw t, int throwResult);

	public void setDeadType(Throw t, int deadType);

	public void setOwnGoals(Throw t, boolean[] ownGoals);

	public int[] getFinalScores(Throw t);

	public void setInitialScores(Throw t);

	public void setInitialScores(Throw t, Throw previousThrow);

	public void setDefErrors(Throw t, boolean[] defErrors);

	public int[] getScoreDifferentials(Throw t);

	public boolean isDropScoreBlocked(Throw t);

	public boolean isOffensiveError(Throw t);

	public boolean isStackHit(Throw t);

	public boolean isOnFire(Throw t);

	public boolean isFiredOn(Throw t);

	public boolean stokesOffensiveFire(Throw t);

	public boolean quenchesOffensiveFire(Throw t);

	public boolean quenchesDefensiveFire(Throw t);

	public void setIsTipped(Throw t, boolean isTipped);

	public void setFireCounts(Throw t, Throw previousThrow);

	public void setFireCounts(Throw t, int[] fireCounts);

	public void setOffenseFireCount(Throw t, int offenseFireCount);

	public void setDefenseFireCount(Throw t, int defenseFireCount);

	public String getSpecialString(Throw t);

	public void setThrowDrawable(Throw t, ImageView iv);

	public boolean isValid(Throw t, Context context);

	public boolean isValid(Throw t);
}