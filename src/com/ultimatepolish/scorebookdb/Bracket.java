package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ultimatepolish.polishscorebook.R;

public class Bracket implements View.OnClickListener {
	public static String LOGTAG = "Bracket";
	public Context context;
	private Session s;
	public RelativeLayout rl;
	private List<SessionMember> sMembers = new ArrayList<SessionMember>();
	private Boolean isDoubleElim;
	private BracketData wBr;
	private BracketData lBr;

	// sMemberMap maps a player id to a session member
	public HashMap<Long, SessionMember> sMemberMap = new HashMap<Long, SessionMember>();

	Dao<Session, Long> sDao;
	Dao<SessionMember, Long> smDao;
	Dao<Player, Long> pDao;
	Dao<Game, Long> gDao;

	public Bracket(ScrollView sv, Session s, Boolean isDoubleElim) {
		super();
		this.context = sv.getContext();
		this.s = s;
		this.isDoubleElim = isDoubleElim;

		try {
			sDao = Session.getDao(context);
			smDao = SessionMember.getDao(context);
			pDao = Player.getDao(context);
			gDao = Game.getDao(context);

			// get all the session members
			QueryBuilder<Session, Long> sQue = sDao.queryBuilder();
			sQue.where().eq("id", s.getId());
			QueryBuilder<SessionMember, Long> smQue = smDao.queryBuilder();
			sMembers = smQue.join(sQue)
					.orderBy(SessionMember.PLAYER_SEED, true).query();
			for (SessionMember member : sMembers) {
				pDao.refresh(member.getPlayer());
				sMemberMap.put(member.getPlayer().getId(), member);
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		if (isDoubleElim) {
			for (int ii = 0; ii < 2 * sMembers.size(); ii++) {
				lBr.addMatch(ii);
			}
		}

		createSingleElimBracket();
	}

	private void createSingleElimBracket() {
		// matches and tiers are 0 based.
		// matches are numbered top to bottom starting at tier 0 and continuing
		// in higher tiers of winners bracket followed by losers bracket.

		// Id for upper half of match = 1000 + matchId
		// Id for lower half of match = 2000 + matchId

		rl = new RelativeLayout(context);
		this.context = rl.getContext();
		RelativeLayout.LayoutParams lp;
		TextView tv;
		Integer matchIdx;

		SessionMember byeMember = new SessionMember(SMType.BYE, -1000);
		SessionMember unsetMember = new SessionMember(SMType.UNSET, -1000);

		// seed the winners bracket
		foldRoster();
		wBr = new BracketData(sMembers.size());
		for (Integer ii = 0; ii < sMembers.size(); ii += 2) {
			if (sMembers.get(ii + 1).getSeed() == SMType.BYE) {
				wBr.modSm12(ii / 2, ii, SMType.TIP, ii + 1, SMType.BYE);
			} else {
				wBr.modSm12(ii / 2, ii, SMType.TIP, ii + 1, SMType.TIP);
			}
		}
		wBr.byeBye();

		makeInvisibleHeaders(rl);

		for (Integer mPos = 0; mPos < wBr.length(); mPos++) {
			// populate the bracket map
			matchIdx = wBr.matchIds.get(mPos);

			// upper half of match
			tv = makeHalfBracket(wBr, matchIdx, true);
			if (wBr.sm1Types.get(mPos) == SMType.TIP) {
				addViewToLayout(tv, true);
			} else {
				addViewToLayout(tv, false);
			}

			// lower half of match
			if (wBr.sm2Types.get(mPos) != SMType.NA) {
				tv = makeHalfBracket(wBr, matchIdx, false);
				if (wBr.sm2Types.get(mPos) == SMType.TIP) {
					addViewToLayout(tv, true);
				} else {
					addViewToLayout(tv, false);
				}
			}
		}
	}

	public void refreshSingleElimBracket() {
		TextView tv;

		// get all the completed games for the session, ordered by date played
		List<Game> sGamesList = new ArrayList<Game>();
		try {
			Log.i(LOGTAG, "session id is " + s.getId());
			sGamesList = gDao.queryBuilder().orderBy(Game.DATE_PLAYED, true)
					.where().eq(Game.SESSION, s.getId()).query();
			for (Game g : sGamesList) {
				gDao.refresh(g);
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		// step through the games for this session and associate each with a
		// match in the bracket
		int smIdxA;
		int smIdxB;

		for (Game g : sGamesList) {
			smIdxA = sMembers.indexOf(sMemberMap
					.get(g.getFirstPlayer().getId()));
			smIdxB = sMembers.indexOf(sMemberMap.get(g.getSecondPlayer()
					.getId()));
			wBr.matchMatch(g.getId(), smIdxA, smIdxB);

			if (g.getIsComplete()) {
				smIdxA = sMembers
						.indexOf(sMemberMap.get(g.getWinner().getId()));
				wBr.promoteWinner(wBr.gameIds.indexOf(g.getId()), smIdxA);
			}
		}

		// now refresh the views
		int matchId;
		int viewId;
		int smColor;
		int smType;
		for (int idx = 0; idx < wBr.length(); idx++) {
			matchId = wBr.matchIds.get(idx);

			// match upper view
			viewId = matchId + SMType.UPPER;
			smType = wBr.sm1Types.get(idx);
			tv = (TextView) rl.findViewById(viewId);
			switch (smType) {
			case SMType.BYE:
				tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
				break;
			case SMType.LOSS:
				smColor = sMembers.get(wBr.sm1Idcs.get(idx)).getPlayer().color;
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.WIN:
				smColor = sMembers.get(wBr.sm1Idcs.get(idx)).getPlayer().color;
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.TIP:
				smColor = sMembers.get(wBr.sm1Idcs.get(idx)).getPlayer().color;
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			}

			// match lower view
			viewId = matchId + SMType.LOWER;
			smType = wBr.sm2Types.get(idx);
			tv = (TextView) rl.findViewById(viewId);
			switch (smType) {
			case SMType.BYE:
				tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
				break;
			case SMType.LOSS:
				smColor = sMembers.get(wBr.sm2Idcs.get(idx)).getPlayer().color;
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.WIN:
				smColor = sMembers.get(wBr.sm2Idcs.get(idx)).getPlayer().color;
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.TIP:
				smColor = sMembers.get(wBr.sm2Idcs.get(idx)).getPlayer().color;
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			}
		}
	}

	public void foldRoster() {
		// expand the list size to the next power of two
		Integer n = factorTwos(sMembers.size());

		SessionMember dummySessionMember = new SessionMember(SMType.BYE, -1000);

		while (sMembers.size() < Math.pow(2, n)) {
			sMembers.add(dummySessionMember);
		}
		List<SessionMember> tempRoster = new ArrayList<SessionMember>();
		for (Integer i = 0; i < n - 1; i++) {
			tempRoster.clear();
			for (Integer j = 0; j < sMembers.size() / Math.pow(2, i + 1); j++) {
				tempRoster.addAll(sMembers.subList(j * (int) Math.pow(2, i),
						(j + 1) * (int) Math.pow(2, i)));
				tempRoster.addAll(sMembers.subList(sMembers.size() - (j + 1)
						* (int) Math.pow(2, i), sMembers.size() - (j)
						* (int) Math.pow(2, i)));
			}
			sMembers.clear();
			sMembers.addAll(tempRoster);
		}
	}

	public Integer factorTwos(Integer rosterSize) {
		Integer n = 1;
		while (Math.pow(2, n) < rosterSize) {
			n++;
		}
		return n;
	}

	public void makeInvisibleHeaders(RelativeLayout rl) {
		// invisible headers are for spacing the bracket.
		TextView tv;
		RelativeLayout.LayoutParams lp;

		// header for the labeled brackets on tier 0
		tv = new TextView(context);
		tv.setWidth(350);
		tv.setHeight(0);
		tv.setId(1);
		tv.setBackgroundColor(Color.BLACK);
		lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		rl.addView(tv, lp);

		// headers for the remaining tiers
		Integer nTiers = factorTwos(sMembers.size());

		Integer tierWidth = 150;

		// tier width = (screen width - label width - arbitrary side spacing) /
		// number of tiers
		// tierWidth = (svWidth - 350 - 100) / nTiers;

		for (Integer i = 0; i < nTiers; i++) {
			tv = new TextView(context);
			tv.setWidth(tierWidth);
			tv.setHeight(0);
			tv.setId(i + 2);
			tv.setBackgroundColor(Color.RED);
			lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
			lp.addRule(RelativeLayout.RIGHT_OF, i + 1);
			lp.setMargins(-14, 0, 0, 0);
			rl.addView(tv, lp);
		}
	}

	public TextView makeHalfBracket(BracketData bd, Integer matchId,
			Boolean upper) {
		TextView tv = new TextView(context);

		int idx = bd.matchIds.indexOf(matchId);
		int smType = SMType.TIP;
		SessionMember sm = null;

		if (upper) {
			tv.setId(matchId + SMType.UPPER);
			smType = bd.sm1Types.get(idx);
			switch (smType) {
			case SMType.TIP:
				sm = sMembers.get(bd.sm1Idcs.get(idx));
				tv.setText("(" + String.valueOf(sm.getSeed() + 1) + ") "
						+ sm.getPlayer().getNickName());
				tv.setBackgroundResource(R.drawable.bracket_top_labeled);
				tv.getBackground().setColorFilter(sm.getPlayer().getColor(),
						Mode.MULTIPLY);
				break;
			case SMType.UNSET:
				tv.setBackgroundResource(R.drawable.bracket_top);
				tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
				break;
			}
			if (bd.sm2Types.get(idx) == SMType.NA) {
				tv.setBackgroundResource(R.drawable.bracket_endpoint);
				tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
			}
		} else {
			tv.setId(matchId + SMType.LOWER);
			smType = bd.sm2Types.get(idx);
			switch (smType) {
			case SMType.TIP:
				sm = sMembers.get(bd.sm2Idcs.get(idx));
				tv.setText("(" + String.valueOf(sm.getSeed() + 1) + ") "
						+ sm.getPlayer().getNickName());
				tv.setBackgroundResource(R.drawable.bracket_bottom_labeled);
				tv.getBackground().setColorFilter(sm.getPlayer().getColor(),
						Mode.MULTIPLY);
				break;
			case SMType.UNSET:
				tv.setBackgroundResource(R.drawable.bracket_bottom);
				tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
				break;
			}
		}
		tv.setGravity(Gravity.RIGHT);
		tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);

		tv.setOnClickListener(this);

		return tv;
	}

	public Integer getTier(Integer bracketIdx) {
		// can take bracket idx or match idx
		Integer matchIdx = bracketIdx % 1000;
		return ((Double) Math.floor(-Math.log(1 - ((double) matchIdx)
				/ sMembers.size())
				/ Math.log(2))).intValue();
	}

	public Integer getTopMatchOfTier(Integer tier) {
		return (int) (sMembers.size() * (1 - Math.pow(2, -tier)));
	}

	public Integer findViewAbove(BracketData bd, Integer viewId) {
		Integer matchId = viewId % SMType.MOD;
		Integer viewAboveId = 1;

		if (!isUpperView(viewId)) {
			viewAboveId = matchId + SMType.UPPER;
		} else {
			Integer baseId = matchId;
			if (getTier(matchId) > 0) {
				baseId = getTopParentMatch(matchId);
			}
			if (bd.matchIds.contains(baseId) && baseId != matchId) {
				viewAboveId = baseId + SMType.UPPER;
			} else {
				if (baseId > getTopMatchOfTier(getTier(baseId))) {
					// have to keep track of upper/lower now
					baseId += SMType.LOWER - 1; // lower arm of the match above
					while (!bd.matchIds.contains(baseId % SMType.MOD)) {
						baseId = getChildBracketId(baseId);
					}
					viewAboveId = baseId;
				}
			}
		}

		Log.i(LOGTAG, "viewId: " + viewId + " placed below " + viewAboveId);
		return viewAboveId;
	}

	public boolean isUpperView(int viewId) {
		assert viewId >= 1000;
		if (viewId < 2000) {
			return true;
		} else {
			return false;
		}
	}

	public Integer getTopParentMatch(Integer bracketIdx) {
		// can take bracket idx or match idx
		Integer matchIdx = bracketIdx % 1000;
		Integer tier = getTier(matchIdx);
		Integer topOfTier = getTopMatchOfTier(tier);
		Integer topOfPrevTier = getTopMatchOfTier(tier - 1);

		Integer topParentMatch = topOfPrevTier + 2 * (matchIdx - topOfTier);
		return topParentMatch;
	}

	public Integer getChildBracketId(Integer bracketIdx) {
		// this can take in a bracket or match idx
		Integer matchIdx = bracketIdx % 1000;
		Integer tier = getTier(matchIdx);
		Integer topOfTier = getTopMatchOfTier(tier);
		Integer topOfNextTier = getTopMatchOfTier(tier + 1);

		Integer childBracket = topOfNextTier + (matchIdx - topOfTier) / 2
				+ 1000;
		if (matchIdx % 2 != 0) {
			childBracket += 1000;
		}
		return childBracket;
	}

	private void addViewToLayout(TextView tv, Boolean isLabeled) {
		Integer matchId = tv.getId() % SMType.MOD;
		Boolean upper = isUpperView(tv.getId());
		Integer tier = getTier(matchId);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (!isLabeled) {
			lp.addRule(RelativeLayout.ALIGN_LEFT, tier + 1);
			if (tier == getTier(sMembers.size() - 1)) {
				lp.setMargins(0, -25, 0, 0);
			} else if (upper) {
				Integer topParentMatch = getTopParentMatch(matchId);
				lp.addRule(RelativeLayout.ALIGN_BOTTOM, topParentMatch
						+ SMType.LOWER);
				lp.setMargins(0, -2, 0, 0);
			} else {
				Integer bottomParentMatch = getTopParentMatch(matchId) + 1;
				lp.addRule(RelativeLayout.ABOVE, bottomParentMatch
						+ SMType.LOWER);
				lp.setMargins(0, 0, 0, -2);
			}

		} else {
			if (upper) {
				lp.setMargins(0, 8, 0, 0);
			} else {
				lp.setMargins(0, 0, 0, 8);
			}
		}

		lp.addRule(RelativeLayout.ALIGN_RIGHT, tier + 1);
		lp.addRule(RelativeLayout.BELOW, findViewAbove(wBr, tv.getId()));

		rl.addView(tv, lp);
	}

	@Override
	public void onClick(View v) {
		Log.i(LOGTAG, "View " + v.getId() + " was clicked");
	}

	public MatchInfo getMatchInfo(int viewId) {
		MatchInfo mInfo = new MatchInfo();
		int matchId = viewId % SMType.MOD;
		if (wBr.matchIds.contains(matchId)) {
			int idx = wBr.matchIds.indexOf(matchId);
			mInfo = new MatchInfo(wBr, idx);
		}
		return mInfo;
	}

	private class BracketData {
		public boolean isDoubleElim;
		private int matchIdOffset = 0;
		private int nLeafs;
		private List<Integer> matchIds = new ArrayList<Integer>();
		private List<Integer> sm1Idcs = new ArrayList<Integer>();
		private List<Integer> sm1Types = new ArrayList<Integer>();
		private List<Integer> sm2Idcs = new ArrayList<Integer>();
		private List<Integer> sm2Types = new ArrayList<Integer>();
		private List<Long> gameIds = new ArrayList<Long>();

		public BracketData(int nLeafs) {
			this(nLeafs, 0);
		}

		public BracketData(int nLeafs, int offset) {
			this.nLeafs = nLeafs;
			matchIdOffset = offset;
			for (int ii = 0; ii < nLeafs; ii++) {
				addMatch(ii);
			}
			sm2Types.set(nLeafs - 1, SMType.NA);
		}

		public void addMatch(int matchId) {
			addMatch(matchId, -1, SMType.UNSET, -1, SMType.UNSET);
		}

		public void addMatch(int matchId, int sm1Idx, int sm1Type, int sm2Idx,
				int sm2Type) {
			matchIds.add(matchId);
			sm1Idcs.add(sm1Idx);
			sm1Types.add(sm1Type);
			sm2Idcs.add(sm2Idx);
			sm2Types.add(sm2Type);
			gameIds.add((long) -1);
		}

		private void removeMatch(int pos) {
			matchIds.remove(pos);
			sm1Idcs.remove(pos);
			sm1Types.remove(pos);
			sm2Idcs.remove(pos);
			sm2Types.remove(pos);
			gameIds.remove(pos);
		}

		public void modSm12(int matchId, int sm1Idx, int sm1Type, int sm2Idx,
				int sm2Type) {
			Integer idx = matchIds.indexOf(matchId);
			if (idx != -1) {
				sm1Idcs.set(idx, sm1Idx);
				sm1Types.set(idx, sm1Type);
				sm2Idcs.set(idx, sm2Idx);
				sm2Types.set(idx, sm2Type);
			}
		}

		public void byeBye() {
			// get rid of bye matches
			int childViewId;
			int childMatchId;

			// promote players with a bye
			for (int ii = 0; ii < matchIds.size(); ii++) {
				if (sm2Types.get(ii) == SMType.BYE) {
					childViewId = getChildBracketId(matchIds.get(ii));
					childMatchId = childViewId % SMType.MOD;
					assert matchIds.indexOf(childMatchId) == childMatchId;

					if (isUpperView(childViewId)) {
						sm1Idcs.set(childMatchId, sm1Idcs.get(ii));
						sm1Types.set(childMatchId, sm1Types.get(ii));
					} else {
						sm2Idcs.set(childMatchId, sm1Idcs.get(ii));
						sm2Types.set(childMatchId, sm1Types.get(ii));
					}
					sm1Types.set(ii, SMType.BYE);
				}
			}

			// now go back through and remove all matches with two bye players
			for (int ii = matchIds.size() - 1; ii >= 0; ii--) {
				if (sm1Types.get(ii) == SMType.BYE
						&& sm2Types.get(ii) == SMType.BYE) {
					removeMatch(ii);
				}
			}

			Log.i("Crunch", "Final list:");
			for (int ii = 0; ii < matchIds.size(); ii++) {
				Log.i("Crunch", "mIdx: " + matchIds.get(ii) + ", sm1id: "
						+ sm1Idcs.get(ii) + ", sm1type: " + sm1Types.get(ii)
						+ ", sm2id: " + sm2Idcs.get(ii) + ", sm2type: "
						+ sm1Types.get(ii) + ", gId: " + gameIds.get(ii));
			}
		}

		public void matchMatch(long gId, int smIdxA, int smIdxB) {
			if (gameIds.contains(gId)) {
				int idx = gameIds.indexOf(gId);
				assert hasSm(idx, smIdxA) && hasSm(idx, smIdxB);
			} else {
				int nMatches = length();
				for (int idx = 0; idx < nMatches; idx++) {
					if (hasSm(idx, smIdxA) && hasSm(idx, smIdxB)
							&& gameIds.get(idx) == -1) {
						Log.i(LOGTAG, "Matching game " + gId + " to match "
								+ matchIds.get(idx));
						gameIds.set(idx, gId);
						break;
					}
				}
			}
		}

		public void promoteWinner(int idx, int wIdx) {
			assert gameIds.get(idx) != -1;
			boolean sm1Wins = true;

			if (wIdx == sm2Idcs.get(idx)) {
				sm1Wins = false;
			} else {
				assert wIdx == sm1Idcs.get(idx);
			}

			if (sm1Wins) {
				sm1Types.set(idx, SMType.WIN);
				sm2Types.set(idx, SMType.LOSS);
				wIdx = sm1Idcs.get(idx);
			} else {
				sm1Types.set(idx, SMType.LOSS);
				sm2Types.set(idx, SMType.WIN);
				wIdx = sm2Idcs.get(idx);
			}

			int childViewId = getChildBracketId(matchIds.get(idx));
			int childIdx = matchIds.indexOf(childViewId % SMType.MOD);

			if (isUpperView(childViewId)) {
				sm1Idcs.set(childIdx, wIdx);
				sm1Types.set(childIdx, SMType.TIP);
			} else {
				sm2Idcs.set(childIdx, wIdx);
				sm2Types.set(childIdx, SMType.TIP);
			}
		}

		private Boolean hasSm(int idx, int smIdx) {
			if (sm1Idcs.get(idx) == smIdx || sm2Idcs.get(idx) == smIdx) {
				return true;
			} else {
				return false;
			}
		}

		public int length() {
			assert matchIds.size() == sm1Idcs.size();
			assert matchIds.size() == sm1Types.size();
			assert matchIds.size() == sm2Idcs.size();
			assert matchIds.size() == sm2Types.size();
			assert matchIds.size() == gameIds.size();
			return matchIds.size();
		}
	}

	public class MatchInfo {
		public long gameId = -1;
		public boolean allowCreate = false;
		public boolean allowView = false;
		public String marquee = "";

		MatchInfo() {
		}

		MatchInfo(BracketData bd, int idx) {
			gameId = bd.gameIds.get(idx);
			int sm1Type = bd.sm1Types.get(idx);
			int sm2Type = bd.sm2Types.get(idx);
			if (sm1Type == SMType.TIP && sm2Type == SMType.TIP) {
				allowCreate = true;
			}
			if (sm1Type == SMType.WIN || sm1Type == SMType.LOSS) {
				assert sm2Type == SMType.WIN || sm2Type == SMType.LOSS;
				allowView = true;
			}

			marquee += "[ " + bd.matchIds.get(idx) + " / " + gameId + " ] ";

			// upper player
			if (sm1Type == SMType.UNSET) {
				marquee += "Unknown";
			} else {
				marquee += sMembers.get(bd.sm1Idcs.get(idx)).getPlayer()
						.getNickName();
				if (sm1Type == SMType.WIN) {
					marquee += " (W)";
				} else if (sm1Type == SMType.LOSS) {
					marquee += " (L)";
				}
			}

			// lower player
			if (sm2Type == SMType.UNSET) {
				marquee += " -vs- Unknown";
			} else if (sm2Type == SMType.NA) {
				marquee += ", tournament winner.";
			} else {
				marquee += " -vs- "
						+ sMembers.get(bd.sm2Idcs.get(idx)).getPlayer()
								.getNickName();
				if (sm2Type == SMType.WIN) {
					marquee += " (W)";
				} else if (sm2Type == SMType.LOSS) {
					marquee += " (L)";
				}
			}
		}
	}

	private final class SMType {
		public static final int TIP = 0;
		public static final int WIN = 1;
		public static final int LOSS = 2;
		public static final int BYE = 3;
		public static final int UNSET = 4;
		public static final int RESPAWN = 5;
		public static final int NA = 7;
		public static final int UPPER = 1000;
		public static final int LOWER = 2000;
		public static final int MOD = 1000;
	}
}
