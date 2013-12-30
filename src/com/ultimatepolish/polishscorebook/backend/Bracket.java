package com.ultimatepolish.polishscorebook.backend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.ultimatepolish.db.Game;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.db.Session;
import com.ultimatepolish.db.SessionMember;
import com.ultimatepolish.polishscorebook.R;

public class Bracket implements View.OnClickListener {
	public static String LOGTAG = "Bracket";
	public Context context;
	private Session s;
	public RelativeLayout rl;
	private List<SessionMember> sMembers = new ArrayList<SessionMember>();
	private Boolean isDoubleElim;
	private BracketData wBr; // winners bracket
	private BracketData lBr; // losers bracket
	private BracketData fBr; // finals bracket

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

		rl = new RelativeLayout(context);
		this.context = rl.getContext();

		foldRoster();
		wBr = new BracketData(sMembers.size());
		wBr.seed(sMembers);
		buildWinnersBracket();

		if (isDoubleElim) {

			lBr = new BracketData(sMembers.size());
			lBr.changeOffsets(factorTwos(sMembers.size()) + 1, sMembers.size());
			lBr.seed(sMembers);
			buildLosersBracket();
		}
	}

	private void buildWinnersBracket() {
		// matches and tiers are 0 based.
		// matches are numbered top to bottom starting at tier 0 and continuing
		// in higher tiers of winners bracket followed by losers bracket.

		// Id for upper half of match = matchId + SMType.UPPER
		// Id for lower half of match = matchId + SMType.LOWER

		TextView tv;
		Integer matchIdx;

		// lay out the bracket
		makeInvisibleHeaders(350, 150, 0, 0, 0);

		for (Integer mPos = 0; mPos < wBr.length(); mPos++) {
			// populate the bracket map
			matchIdx = wBr.matchIds.get(mPos);

			// upper half of match
			tv = makeHalfBracket(wBr, matchIdx, true);
			if (wBr.sm1Types.get(mPos) == SMType.TIP) {
				addViewToLayout(wBr, tv, true);
			} else {
				addViewToLayout(wBr, tv, false);
			}

			// lower half of match
			if (wBr.sm2Types.get(mPos) != SMType.NA) {
				tv = makeHalfBracket(wBr, matchIdx, false);
				if (wBr.sm2Types.get(mPos) == SMType.TIP) {
					addViewToLayout(wBr, tv, true);
				} else {
					addViewToLayout(wBr, tv, false);
				}
			}
		}
	}

	private void buildLosersBracket() {
		// matches and tiers are 0 based.
		// matches are numbered top to bottom starting at tier 0 and continuing
		// in higher tiers of winners bracket followed by losers bracket.

		// Id for upper half of match = matchId + SMType.UPPER
		// Id for lower half of match = matchId + SMType.LOWER

		TextView tv;
		Integer matchIdx;

		// lay out the bracket
		int wBrLowest;
		if (wBr.matchIds.contains(sMembers.size() / 2)) {
			wBrLowest = sMembers.size() / 2 - 1 + SMType.LOWER;
		} else {
			wBrLowest = wBr.findViewAbove(sMembers.size() / 2 - 1
					+ SMType.LOWER);
		}
		makeInvisibleHeaders(350, 150, 1, wBrLowest, lBr.headerIdOffset);

		for (Integer mPos = 0; mPos < lBr.length(); mPos++) {
			// populate the bracket map
			matchIdx = lBr.matchIds.get(mPos);

			// upper half of match
			tv = makeHalfBracket(lBr, matchIdx, true);
			if (lBr.sm1Types.get(mPos) == SMType.TIP) {
				addViewToLayout(lBr, tv, true);
			} else {
				addViewToLayout(lBr, tv, false);
			}

			// lower half of match
			if (lBr.sm2Types.get(mPos) != SMType.NA) {
				tv = makeHalfBracket(lBr, matchIdx, false);
				if (lBr.sm2Types.get(mPos) == SMType.TIP) {
					addViewToLayout(lBr, tv, true);
				} else {
					addViewToLayout(lBr, tv, false);
				}
			}
		}
	}

	public void refreshWinnersBracket() {
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
			wBr.matchMatches(g.getId(), smIdxA, smIdxB);

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
		boolean isLabeled;
		for (int idx = 0; idx < wBr.length(); idx++) {
			matchId = wBr.matchIds.get(idx);

			// match upper view
			viewId = matchId + SMType.UPPER;
			smType = wBr.sm1Types.get(idx);
			tv = (TextView) rl.findViewById(viewId);
			isLabeled = tv.getText() != "";
			if (isLabeled && wBr.smLost(wBr.sm1Idcs.get(idx))) {
				tv.setPaintFlags(tv.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
			}
			switch (smType) {
			case SMType.BYE:
				tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
				break;
			case SMType.LOSS:
				smColor = sMembers.get(wBr.sm1Idcs.get(idx)).getPlayer().color;
				if (isLabeled) {
					tv.setBackgroundResource(R.drawable.bracket_top_eliminated_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_top_eliminated);
				}
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.WIN:
				smColor = sMembers.get(wBr.sm1Idcs.get(idx)).getPlayer().color;
				if (isLabeled) {
					tv.setBackgroundResource(R.drawable.bracket_top_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_top);
				}
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.TIP:
				smColor = sMembers.get(wBr.sm1Idcs.get(idx)).getPlayer().color;
				if (wBr.sm2Types.get(idx) == SMType.NA) {
				} else if (isLabeled) {
					tv.setBackgroundResource(R.drawable.bracket_top_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_top);
				}
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			}

			// match lower view
			viewId = matchId + SMType.LOWER;
			smType = wBr.sm2Types.get(idx);
			tv = (TextView) rl.findViewById(viewId);
			if (smType != SMType.NA) {
				isLabeled = tv.getText() != "";
				if (isLabeled && wBr.smLost(wBr.sm2Idcs.get(idx))) {
					tv.setPaintFlags(tv.getPaintFlags()
							| Paint.STRIKE_THRU_TEXT_FLAG);
				}
			}
			switch (smType) {
			case SMType.BYE:
				tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
				break;
			case SMType.LOSS:
				smColor = sMembers.get(wBr.sm2Idcs.get(idx)).getPlayer().color;
				if (isLabeled) {
					tv.setBackgroundResource(R.drawable.bracket_bottom_eliminated_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_bottom_eliminated);
				}
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.WIN:
				smColor = sMembers.get(wBr.sm2Idcs.get(idx)).getPlayer().color;
				if (isLabeled) {
					tv.setBackgroundResource(R.drawable.bracket_bottom_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_bottom);
				}
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			case SMType.TIP:
				smColor = sMembers.get(wBr.sm2Idcs.get(idx)).getPlayer().color;
				if (isLabeled) {
					tv.setBackgroundResource(R.drawable.bracket_bottom_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_bottom);
				}
				tv.getBackground().setColorFilter(smColor, Mode.MULTIPLY);
				break;
			}
		}
	}

	public void makeInvisibleHeaders(int baseWidth, int tierWidth,
			int rightOfId, int belowId, int offset) {
		// invisible headers are for spacing the bracket.
		TextView tv;
		RelativeLayout.LayoutParams lp;
		int vwHeight = 10;
		Log.i(LOGTAG, "baseWidth: " + baseWidth + ", tierWidth: " + tierWidth
				+ ", rightOfId: " + rightOfId + ", belowId: " + belowId
				+ ", offset: " + offset);

		// header for the labeled brackets on tier 0
		tv = new TextView(context);
		tv.setWidth(baseWidth);
		tv.setHeight(vwHeight);
		tv.setId(1 + offset);
		tv.setBackgroundColor(Color.BLACK);
		lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		if (belowId > 0) {
			Log.i(LOGTAG, "Header is below " + belowId);
			lp.addRule(RelativeLayout.BELOW, belowId);
		}
		rl.addView(tv, lp);

		// headers for the remaining tiers
		Integer nTiers = factorTwos(sMembers.size());

		// tier width = (screen width - label width - arbitrary side spacing) /
		// number of tiers
		// tierWidth = (svWidth - 350 - 100) / nTiers;

		int[] vwColor = { Color.RED, Color.BLUE, Color.GREEN };
		for (Integer i = 0; i < nTiers; i++) {
			tv = new TextView(context);
			tv.setWidth(tierWidth);
			tv.setHeight(vwHeight);
			tv.setId(i + 2 + offset);
			tv.setBackgroundColor(vwColor[i % 3]);
			lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_BASELINE, 1 + offset);
			lp.addRule(RelativeLayout.RIGHT_OF, i + 1 + offset);
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
			tv.setId(matchId + bd.matchIdOffset + SMType.UPPER);
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
			tv.setId(matchId + bd.matchIdOffset + SMType.LOWER);
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

	private void addViewToLayout(BracketData bd, TextView tv, Boolean isLabeled) {
		Integer matchId = tv.getId() % SMType.MOD;
		Boolean upper = isUpperView(tv.getId());
		Integer tier = bd.getTier(matchId);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (!isLabeled) {
			lp.addRule(RelativeLayout.ALIGN_LEFT, tier + 1);
			if (tier == bd.getTier(sMembers.size() - 1)) {
				lp.setMargins(0, -25, 0, 0);
			} else if (upper) {
				Integer topParentMatch = bd.getTopParentMatch(matchId);
				lp.addRule(RelativeLayout.ALIGN_BOTTOM, topParentMatch
						+ SMType.LOWER);
				lp.setMargins(0, -2, 0, 0);
			} else {
				Integer bottomParentMatch = bd.getTopParentMatch(matchId) + 1;
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
		lp.addRule(RelativeLayout.BELOW, bd.findViewAbove(tv.getId()));

		rl.addView(tv, lp);
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

	/** find n such that 2**n >= p */
	public Integer factorTwos(int p) {
		Integer n = 1;
		while (Math.pow(2, n) < p) {
			n++;
		}
		return n;
	}

	public boolean isUpperView(int viewId) {
		assert viewId >= 1000;
		if (viewId < 2000) {
			return true;
		} else {
			return false;
		}
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
		private int headerIdOffset = 0;
		private int matchIdOffset = 0;
		private int nLeafs;
		private List<Integer> matchIds = new ArrayList<Integer>();
		private List<Integer> sm1Idcs = new ArrayList<Integer>();
		private List<Integer> sm1Types = new ArrayList<Integer>();
		private List<Integer> sm2Idcs = new ArrayList<Integer>();
		private List<Integer> sm2Types = new ArrayList<Integer>();
		private List<Long> gameIds = new ArrayList<Long>();

		public BracketData(int nLeafs) {
			this.nLeafs = (int) Math.pow(2, factorTwos(nLeafs));
			addMatches();
		}

		private void addMatches() {
			for (int ii = 0; ii < nLeafs; ii++) {
				matchIds.add(ii);
				sm1Idcs.add(-1);
				sm1Types.add(SMType.UNSET);
				sm2Idcs.add(-1);
				sm2Types.add(SMType.UNSET);
				gameIds.add((long) -1);
			}
			sm2Types.set(nLeafs - 1, SMType.NA);
		}

		private void removeMatch(int pos) {
			matchIds.remove(pos);
			sm1Idcs.remove(pos);
			sm1Types.remove(pos);
			sm2Idcs.remove(pos);
			sm2Types.remove(pos);
			gameIds.remove(pos);
		}

		public void changeOffsets(int headerIdOffset, int matchIdOffset) {
			assert matchIdOffset - this.matchIdOffset >= 0;
			this.headerIdOffset = headerIdOffset;
			this.matchIdOffset = matchIdOffset;
		}

		private int getTier(int viewId) {
			// can take bracket idx or match idx
			int matchId = viewId % SMType.MOD;
			return ((Double) Math.floor(-Math.log(1 - ((double) matchId)
					/ nLeafs)
					/ Math.log(2))).intValue();
		}

		public int getTopMatchOfTier(int tier) {
			return (int) (nLeafs * (1 - Math.pow(2, -tier)));
		}

		public int getTopParentMatch(int bracketIdx) {
			// can take bracket idx or match idx
			Integer matchIdx = bracketIdx % SMType.MOD;
			Integer tier = getTier(matchIdx);
			Integer topOfTier = getTopMatchOfTier(tier);
			Integer topOfPrevTier = getTopMatchOfTier(tier - 1);

			Integer topParentMatch = topOfPrevTier + 2 * (matchIdx - topOfTier);
			return topParentMatch;
		}

		public int getChildBracketId(int bracketIdx) {
			// this can take in a bracket or match idx
			Integer matchIdx = bracketIdx % SMType.MOD;
			Integer tier = getTier(matchIdx);
			Integer topOfTier = getTopMatchOfTier(tier);
			Integer topOfNextTier = getTopMatchOfTier(tier + 1);

			Integer childBracket = topOfNextTier + (matchIdx - topOfTier) / 2
					+ SMType.UPPER;
			if (matchIdx % 2 != 0) {
				childBracket += 1000;
			}
			return childBracket;
		}

		public int findViewAbove(int viewId) {
			Integer matchId = viewId % SMType.MOD - matchIdOffset;

			Integer viewAboveId = headerIdOffset + 1;
			Log.i(LOGTAG, "findviewabove, headerOffset is " + headerIdOffset);

			if (!isUpperView(viewId)) {
				viewAboveId = matchId + matchIdOffset + SMType.UPPER;
			} else {
				Integer baseId = matchId;
				if (getTier(matchId) > 0) {
					baseId = getTopParentMatch(matchId);
				}
				if (matchIds.contains(baseId) && baseId != matchId) {
					viewAboveId = baseId + matchIdOffset + SMType.UPPER;
				} else {
					if (baseId > getTopMatchOfTier(getTier(baseId))) {
						// have to keep track of upper/lower now
						baseId += SMType.LOWER - 1; // lower arm of the match
													// above
						while (!matchIds.contains(baseId % SMType.MOD)) {
							baseId = getChildBracketId(baseId);
						}
						viewAboveId = baseId + matchIdOffset;
					}
				}
			}

			Log.i(LOGTAG, "viewId: " + viewId + " placed below " + viewAboveId);
			return viewAboveId;
		}

		private void modSMs(int matchId, int sm1Idx, int sm1Type, int sm2Idx,
				int sm2Type) {
			Integer idx = matchIds.indexOf(matchId);
			if (idx != -1) {
				sm1Idcs.set(idx, sm1Idx);
				sm1Types.set(idx, sm1Type);
				sm2Idcs.set(idx, sm2Idx);
				sm2Types.set(idx, sm2Type);
			}
		}

		public boolean smLost(int smIdx) {
			boolean hasLost = false;
			int idx1 = sm1Idcs.lastIndexOf(smIdx);
			int idx2 = sm2Idcs.lastIndexOf(smIdx);
			if (idx1 > idx2) {
				if (sm1Types.get(idx1) == SMType.LOSS) {
					hasLost = true;
				}
			} else {
				if (sm2Types.get(idx2) == SMType.LOSS) {
					hasLost = true;
				}
			}
			return hasLost;
		}

		public void seed(List<SessionMember> sMembers) {
			// seed the winners bracket
			for (Integer ii = 0; ii < sMembers.size(); ii += 2) {
				if (sMembers.get(ii + 1).getSeed() == SMType.BYE) {
					modSMs(ii / 2, ii, SMType.TIP, ii + 1, SMType.BYE);
				} else {
					modSMs(ii / 2, ii, SMType.TIP, ii + 1, SMType.TIP);
				}
			}
			byeByes();
		}

		private void byeByes() {
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

		public void matchMatches(long gId, int smIdxA, int smIdxB) {
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
		public long p1Id = -1;
		public long p2Id = -1;
		public boolean allowCreate = false;
		public boolean allowView = false;
		public String marquee = "";

		MatchInfo() {
		}

		MatchInfo(BracketData bd, int idx) {
			gameId = bd.gameIds.get(idx);
			int sm1Type = bd.sm1Types.get(idx);
			int sm2Type = bd.sm2Types.get(idx);

			if (sm1Type == SMType.TIP || sm1Type == SMType.WIN
					|| sm1Type == SMType.LOSS) {
				p1Id = sMembers.get(bd.sm1Idcs.get(idx)).getPlayer().getId();
			}

			if (sm2Type == SMType.TIP || sm2Type == SMType.WIN
					|| sm2Type == SMType.LOSS) {
				p2Id = sMembers.get(bd.sm2Idcs.get(idx)).getPlayer().getId();
			}

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
