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

	public int[] getScoreDifferentials();

	public boolean isDropScoreBlocked();

	public boolean isOffensiveError();

	public boolean isStackHit();

	public boolean isOnFire();

	public boolean isFiredOn();

	public boolean stokesOffensiveFire();

	public boolean quenchesOffensiveFire();

	public boolean quenchesDefensiveFire();

	public void toggleIsTipped(Throw t);

	public void setFireCounts(Throw t, Throw previousThrow);

	public void setFireCounts(int[] fireCounts);

	public void setOffenseFireCount(int offenseFireCount);

	public void setDefenseFireCount(int defenseFireCount);

	public String getSpecialString(Throw t);

	public void setThrowDrawable(Throw t, ImageView iv);

	public boolean isValid(Context context);

	public boolean isValid(Throw t);

	public String getInvalidMessage();
}