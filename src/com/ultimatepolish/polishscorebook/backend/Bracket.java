package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ultimatepolish.db.Game;
import com.ultimatepolish.db.SessionMember;
import com.ultimatepolish.enums.BrDrawable;
import com.ultimatepolish.enums.BrNodeType;

public class Bracket {
	public static String LOGTAG = "Bracket";

	public boolean isDoubleElim;
	private int headerIdOffset = 0;
	private int matchIdOffset = 0;
	private int nLeafs;
	private Map<Long, Integer> smIdMap = new HashMap<Long, Integer>();
	private Map<Integer, SessionMember> smSeedMap = new HashMap<Integer, SessionMember>();
	public List<Integer> matchIds = new ArrayList<Integer>();
	public List<Integer> sm1Idcs = new ArrayList<Integer>();
	public List<Integer> sm1Types = new ArrayList<Integer>();
	public List<Integer> sm2Idcs = new ArrayList<Integer>();
	public List<Integer> sm2Types = new ArrayList<Integer>();
	public List<Long> gameIds = new ArrayList<Long>();
	private RelativeLayout rl;

	public Bracket(List<SessionMember> sMembers, RelativeLayout rl) {
		// fast check that nLeafs is a power of two
		this.nLeafs = sMembers.size();
		this.rl = rl;
		assert (nLeafs & (nLeafs - 1)) == 0;
		seed(sMembers);

		int seed;
		for (SessionMember sm : sMembers) {
			seed = sm.getSeed();
			if (seed >= 0 && !smSeedMap.containsKey(seed)) {
				smIdMap.put(sm.getPlayer().getId(), seed);
				smSeedMap.put(seed, sm);
			}
		}
	}

	/**
	 * Use this constructor for a respawn bracket. ie, members will come from
	 * another bracket after losing or reaching the top tier. The insane loops
	 * generate a tiered losers bracket.
	 */
	public Bracket(int nLeafs, RelativeLayout rl) {
		// fast check that nLeafs is a power of two
		this.nLeafs = 4 * nLeafs;
		this.rl = rl;
		assert (nLeafs & (nLeafs - 1)) == 0;
		seed();

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

	public void buildBracket(Context context) {
		buildBracket(context, 150, -1, -1);
	}

	public void buildBracket(Context context, int tierWidth, int aboveViewId,
			int columnViewId) {
		TextView tv;

		// lay out the bracket
		makeInvisibleHeaders(350, tierWidth, aboveViewId, columnViewId);

		for (Integer mPos = 0; mPos < length(); mPos++) {
			// upper half of match
			tv = makeHalfMatchView(context, mPos, true);
			// tv.setOnClickListener(this);
			if (sm1Types.get(mPos) == BrNodeType.TIP
					|| sm1Types.get(mPos) == BrNodeType.RESPAWN) {
				addViewToLayout(tv, true);
			} else {
				addViewToLayout(tv, false);
			}

			// lower half of match
			if (sm2Types.get(mPos) != BrNodeType.NA) {
				tv = makeHalfMatchView(context, mPos, false);
				if (sm2Types.get(mPos) == BrNodeType.TIP
						|| sm2Types.get(mPos) == BrNodeType.RESPAWN) {
					addViewToLayout(tv, true);
				} else {
					addViewToLayout(tv, false);
				}
			}
		}
	}

	private void makeInvisibleHeaders(int baseWidth, int tierWidth,
			int aboveViewId, int columnViewId) {
		// invisible headers are for spacing the bracket.
		TextView tv;
		RelativeLayout.LayoutParams lp;
		Context context = rl.getContext();

		int vwHeight = 10;
		Log.i(LOGTAG, "baseWidth: " + baseWidth + ", tierWidth: " + tierWidth
				+ ", belowId: " + aboveViewId + ", offset: " + columnViewId);

		// header for the labeled brackets on tier 0
		tv = new TextView(context);
		tv.setWidth(baseWidth);
		tv.setHeight(vwHeight);
		tv.setId(1 + headerIdOffset);
		tv.setBackgroundColor(Color.BLACK);
		lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (columnViewId > 0) {
			lp.addRule(RelativeLayout.ALIGN_RIGHT, columnViewId);
		} else {
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		}
		if (aboveViewId > 0) {
			lp.addRule(RelativeLayout.BELOW, aboveViewId);
		}
		rl.addView(tv, lp);

		// headers for the remaining tiers
		Integer nTiers = factorTwos(nLeafs);

		// tier width = (screen width - label width - arbitrary side spacing) /
		// number of tiers
		// tierWidth = (svWidth - 350 - 100) / nTiers;

		int[] vwColor = { Color.RED, Color.BLUE, Color.GREEN };
		for (Integer i = 0; i < nTiers; i++) {
			tv = new TextView(context);
			lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

			if (headerIdOffset != 0 && i == nTiers - 1) {
				lp.addRule(RelativeLayout.ALIGN_RIGHT, headerIdOffset);
			} else {
				tv.setWidth(tierWidth);
			}
			tv.setHeight(vwHeight);
			tv.setId(i + 2 + headerIdOffset);
			tv.setBackgroundColor(vwColor[i % 3]);

			lp.addRule(RelativeLayout.ALIGN_BASELINE, 1 + headerIdOffset);
			lp.addRule(RelativeLayout.RIGHT_OF, i + 1 + headerIdOffset);
			lp.setMargins(-14, 0, 0, 0);
			rl.addView(tv, lp);
		}
	}

	public TextView makeHalfMatchView(Context context, int idx, Boolean upper) {
		TextView tv = new TextView(context);

		int matchId = matchIds.get(idx);
		int smType = BrNodeType.TIP;
		SessionMember sm = null;
		String drwStr = "";
		int drwColor = Color.LTGRAY;

		if (upper) {
			drwStr += "upper";
			tv.setId(matchId + matchIdOffset + BrNodeType.UPPER);
			smType = sm1Types.get(idx);
			if (smType == BrNodeType.TIP) {
				sm = smSeedMap.get(sm1Idcs.get(idx));
				tv.setText("(" + String.valueOf(sm.getSeed() + 1) + ") "
						+ sm.getPlayer().getNickName());
				drwStr += "_labeled";
				drwColor = sm.getPlayer().getColor();
			} else if (smType == BrNodeType.RESPAWN) {
				tv.setText("(" + (char) (sm1Idcs.get(idx) + 65) + ") ");
				drwStr += "_labeled";
			}
			if (sm2Types.get(idx) == BrNodeType.NA) {
				drwStr = "endpoint";
			}
		} else {
			drwStr += "lower";
			tv.setId(matchId + matchIdOffset + BrNodeType.LOWER);
			smType = sm2Types.get(idx);
			if (smType == BrNodeType.TIP) {
				sm = smSeedMap.get(sm2Idcs.get(idx));
				tv.setText("(" + String.valueOf(sm.getSeed() + 1) + ") "
						+ sm.getPlayer().getNickName());
				drwStr += "_labeled";
				drwColor = sm.getPlayer().getColor();
			} else if (smType == BrNodeType.RESPAWN) {
				tv.setText("(" + (char) (sm2Idcs.get(idx) + 65) + ") ");
				drwStr += "_labeled";
			}
		}
		tv.setBackgroundResource(BrDrawable.map.get(drwStr));
		tv.getBackground().setColorFilter(drwColor, Mode.MULTIPLY);
		tv.setGravity(Gravity.RIGHT);
		tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);

		return tv;
	}

	public void addViewToLayout(TextView tv, Boolean isLabeled) {
		Integer matchId = tv.getId() % BrNodeType.MOD - matchIdOffset;
		Boolean upper = isUpperView(tv.getId() - matchIdOffset);
		Integer tier = getTier(matchId);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (!isLabeled) {
			lp.addRule(RelativeLayout.ALIGN_LEFT, tier + 1 + headerIdOffset);
			if (tier == getTier(nLeafs - 1)) {
				lp.setMargins(0, -25, 0, 0);
			} else if (upper) {
				Integer topParentMatch = getUpperMatchParent(matchId);
				lp.addRule(RelativeLayout.ALIGN_BOTTOM, topParentMatch
						+ matchIdOffset + BrNodeType.LOWER);
				lp.setMargins(0, -2, 0, 0);
			} else {
				Integer bottomParentMatch = getUpperMatchParent(matchId) + 1;
				lp.addRule(RelativeLayout.ABOVE, bottomParentMatch
						+ matchIdOffset + BrNodeType.LOWER);
				lp.setMargins(0, 0, 0, -2);
			}
		} else {
			if (upper) {
				lp.setMargins(0, 8, 0, 0);
			} else {
				lp.setMargins(0, 0, 0, 8);
			}
		}

		lp.addRule(RelativeLayout.ALIGN_RIGHT, tier + 1 + headerIdOffset);
		lp.addRule(RelativeLayout.BELOW, findViewAbove(tv.getId()));

		rl.addView(tv, lp);
	}

	public void refreshViews() {
		TextView tv;
		int matchId;
		int viewId;
		int drwColor;
		String drwString;
		int smType;
		boolean isLabeled;

		for (int idx = 0; idx < length(); idx++) {
			matchId = matchIds.get(idx);

			// match upper view
			viewId = matchId + BrNodeType.UPPER;
			smType = sm1Types.get(idx);
			tv = (TextView) rl.findViewById(viewId);
			drwString = "upper";
			drwColor = Color.LTGRAY;
			isLabeled = tv.getText() != "";

			if (smType != BrNodeType.UNSET) {
				drwColor = smSeedMap.get(sm1Idcs.get(idx)).getPlayer().color;
			}
			if (smType == BrNodeType.LOSS) {
				drwString += "_eliminated";
			}
			if (isLabeled) {
				drwString += "_labeled";
				if (smLost(sm1Idcs.get(idx))) {
					tv.setPaintFlags(tv.getPaintFlags()
							| Paint.STRIKE_THRU_TEXT_FLAG);
				}
			}
			if (sm2Types.get(idx) == BrNodeType.NA) {
				drwString = "endpoint";
			}
			tv.setBackgroundResource(BrDrawable.map.get(drwString));
			tv.getBackground().setColorFilter(drwColor, Mode.MULTIPLY);

			// match lower view
			viewId = matchId + BrNodeType.LOWER;
			smType = sm2Types.get(idx);

			if (smType != BrNodeType.NA) {
				tv = (TextView) rl.findViewById(viewId);
				drwString = "lower";
				drwColor = Color.LTGRAY;
				isLabeled = tv.getText() != "";

				if (smType != BrNodeType.UNSET) {
					drwColor = smSeedMap.get(sm2Idcs.get(idx)).getPlayer().color;
				}
				if (smType == BrNodeType.LOSS) {
					drwString += "_eliminated";
				}
				if (isLabeled) {
					drwString += "_labeled";
					if (smLost(sm2Idcs.get(idx))) {
						tv.setPaintFlags(tv.getPaintFlags()
								| Paint.STRIKE_THRU_TEXT_FLAG);
					}
				}
				tv.setBackgroundResource(BrDrawable.map.get(drwString));
				tv.getBackground().setColorFilter(drwColor, Mode.MULTIPLY);
			}
		}
	}

	private int getTier(int viewId) {
		// can take view idx or match idx
		int matchId = viewId % BrNodeType.MOD;
		return ((Double) Math.floor(-Math.log(1 - ((double) matchId) / nLeafs)
				/ Math.log(2))).intValue();
	}

	private int getTopMatchOfTier(int tier) {
		return (int) (nLeafs * (1 - Math.pow(2, -tier)));
	}

	private int getUpperMatchParent(int bracketIdx) {
		// can take bracket idx or match idx
		Integer matchIdx = bracketIdx % BrNodeType.MOD;
		Integer tier = getTier(matchIdx);
		Integer topOfTier = getTopMatchOfTier(tier);
		Integer topOfPrevTier = getTopMatchOfTier(tier - 1);

		Integer topParentMatch = topOfPrevTier + 2 * (matchIdx - topOfTier);
		return topParentMatch;
	}

	private int getChildViewId(int bracketIdx) {
		// this can take in a bracket or match idx
		Integer matchIdx = bracketIdx % BrNodeType.MOD;
		Integer tier = getTier(matchIdx);
		Integer topOfTier = getTopMatchOfTier(tier);
		Integer topOfNextTier = getTopMatchOfTier(tier + 1);

		Integer childBracket = topOfNextTier + (matchIdx - topOfTier) / 2
				+ BrNodeType.UPPER;
		if (matchIdx % 2 != 0) {
			childBracket += 1000;
		}
		return childBracket;
	}

	public int findViewAbove(int viewId) {
		Integer matchId = viewId % BrNodeType.MOD - matchIdOffset;

		Integer viewAboveId = headerIdOffset + 1;

		if (!isUpperView(viewId)) {
			viewAboveId = matchId + matchIdOffset + BrNodeType.UPPER;
		} else {
			Integer baseId = matchId;
			if (getTier(matchId) > 0) {
				baseId = getUpperMatchParent(matchId);
			}
			if (matchIds.contains(baseId) && baseId != matchId) {
				viewAboveId = baseId + matchIdOffset + BrNodeType.UPPER;
			} else {
				if (baseId > getTopMatchOfTier(getTier(baseId))) {
					// have to keep track of upper/lower now
					baseId += BrNodeType.LOWER - 1; // lower arm of the match
					// above
					while (!matchIds.contains(baseId % BrNodeType.MOD)) {
						baseId = getChildViewId(baseId);
					}
					viewAboveId = baseId + matchIdOffset;
				}
			}
		}

		Log.i(LOGTAG, "viewId: " + viewId + " placed below " + viewAboveId);
		return viewAboveId;
	}

	private boolean isUpperView(int viewId) {
		assert viewId >= 1000;
		if (viewId < 2000) {
			return true;
		} else {
			return false;
		}
	}

	private boolean smLost(int smIdx) {
		boolean hasLost = false;
		int idx1 = sm1Idcs.lastIndexOf(smIdx);
		int idx2 = sm2Idcs.lastIndexOf(smIdx);
		if (idx1 > idx2) {
			if (sm1Types.get(idx1) == BrNodeType.LOSS) {
				hasLost = true;
			}
		} else {
			if (sm2Types.get(idx2) == BrNodeType.LOSS) {
				hasLost = true;
			}
		}
		return hasLost;
	}

	private void seed(List<SessionMember> sMembers) {
		int sm1Seed;
		int sm2Seed;

		// seed the lowest tier
		for (Integer ii = 0; ii < nLeafs; ii += 2) {
			sm1Seed = sMembers.get(ii).getSeed();
			sm2Seed = sMembers.get(ii + 1).getSeed();

			matchIds.add(ii / 2);
			sm1Idcs.add(sm1Seed);
			sm1Types.add(BrNodeType.TIP);
			sm2Idcs.add(sm2Seed);
			if (sm2Seed == BrNodeType.BYE) {
				sm2Types.add(BrNodeType.BYE);
			} else {
				sm2Types.add(BrNodeType.TIP);
			}
			gameIds.add((long) -1);
		}

		// add the rest of the matches
		for (int ii = nLeafs / 2; ii < nLeafs; ii++) {
			matchIds.add(ii);
			sm1Idcs.add(-1);
			sm1Types.add(BrNodeType.UNSET);
			sm2Idcs.add(-1);
			sm2Types.add(BrNodeType.UNSET);
			gameIds.add((long) -1);
		}

		// last match is actually just the winner
		sm2Types.set(nLeafs - 1, BrNodeType.NA);

		byeByes();
	}

	private void seed() {
		List<Integer> idA = new ArrayList<Integer>();
		idA.add(1);
		idA.add(1);
		List<Integer> idB = new ArrayList<Integer>();
		idB.add(2);
		List<Integer> idC = new ArrayList<Integer>();
		idC.add(1);
		idC.add(3);
		idA.addAll(idB);
		idA.addAll(idC);

		int last;
		for (int ii = 1; ii < factorTwos(nLeafs) - 3; ii++) {
			idB.addAll(idB);
			last = idB.size() - 1;
			idB.set(last, (int) (idB.get(last) + Math.pow(2, 2 * ii)));

			idC.addAll(idC);
			last = idC.size() - 1;
			idC.set(last, (int) (idC.get(last) + Math.pow(2, 2 * ii + 1)));

			idA.addAll(idB);
			idA.addAll(idC);
		}

		int matchId = -1;
		int respawnId = 0;
		int tier;
		for (int ii = idA.size() - 1; ii >= 0; ii--) {
			matchId += idA.get(ii);
			matchIds.add(matchId);

			tier = getTier(matchId);

			if (tier % 2 == 0) {
				sm1Idcs.add(respawnId);
				respawnId++;
				sm1Types.add(BrNodeType.RESPAWN);
				sm2Idcs.add(-1);
				if (tier == 0) {
					sm2Types.add(BrNodeType.BYE);
				} else {
					sm2Types.add(BrNodeType.UNSET);
				}
			} else {
				sm1Idcs.add(-1);
				sm1Types.add(BrNodeType.UNSET);
				sm2Idcs.add(-1);
				sm2Types.add(BrNodeType.UNSET);
			}
			gameIds.add((long) -1);
		}

		// last match is actually just the winner
		sm2Types.set(sm2Types.size() - 1, BrNodeType.NA);

		byeByes();
	}

	private void byeByes() {
		// get rid of bye matches
		int childViewId;
		int childMatchId;
		int childIdx;

		Log.i("Crunch", "Initial list:");
		for (int ii = 0; ii < matchIds.size(); ii++) {
			Log.i("Crunch",
					"mIdx: " + matchIds.get(ii) + ", sm1id: " + sm1Idcs.get(ii)
							+ ", sm1type: " + sm1Types.get(ii) + ", sm2id: "
							+ sm2Idcs.get(ii) + ", sm2type: "
							+ sm2Types.get(ii) + ", gId: " + gameIds.get(ii));
		}

		// promote players with a bye
		for (int ii = 0; ii < matchIds.size(); ii++) {
			if (sm2Types.get(ii) == BrNodeType.BYE) {
				childViewId = getChildViewId(matchIds.get(ii));
				childMatchId = childViewId % BrNodeType.MOD;
				childIdx = matchIds.indexOf(childMatchId);
				Log.i(LOGTAG, "ii: " + ii + ", childViewId: " + childViewId);

				if (isUpperView(childViewId)) {
					sm1Idcs.set(childIdx, sm1Idcs.get(ii));
					sm1Types.set(childIdx, sm1Types.get(ii));
				} else {
					sm2Idcs.set(childIdx, sm1Idcs.get(ii));
					sm2Types.set(childIdx, sm1Types.get(ii));
				}
				sm1Types.set(ii, BrNodeType.BYE);
			}
		}

		// now go back through and remove all matches with two bye players
		for (int ii = matchIds.size() - 1; ii >= 0; ii--) {
			if (sm1Types.get(ii) == BrNodeType.BYE
					&& sm2Types.get(ii) == BrNodeType.BYE) {
				removeMatch(ii);
			}
		}

		Log.i("Crunch", "Final list:");
		for (int ii = 0; ii < matchIds.size(); ii++) {
			Log.i("Crunch",
					"mIdx: " + matchIds.get(ii) + ", sm1id: " + sm1Idcs.get(ii)
							+ ", sm1type: " + sm1Types.get(ii) + ", sm2id: "
							+ sm2Idcs.get(ii) + ", sm2type: "
							+ sm1Types.get(ii) + ", gId: " + gameIds.get(ii));
		}
	}

	public List<Game> matchMatches(List<Game> sGames) {
		long gId;
		int smASeed;
		int smBSeed;

		Iterator<Game> gIt = sGames.iterator();
		while (gIt.hasNext()) {
			Game g = gIt.next();
			gId = g.getId();
			smASeed = smIdMap.get(g.getFirstPlayer().getId());
			smBSeed = smIdMap.get(g.getSecondPlayer().getId());

			if (gameIds.contains(gId)) {
				int idx = gameIds.indexOf(gId);
				assert hasSm(idx, smASeed) && hasSm(idx, smBSeed);
				gIt.remove();
			} else {
				int nMatches = length();
				for (int idx = 0; idx < nMatches; idx++) {
					if (hasSm(idx, smASeed) && hasSm(idx, smBSeed)
							&& gameIds.get(idx) == -1) {
						Log.i(LOGTAG, "Matching game " + gId + " to match "
								+ matchIds.get(idx));
						gameIds.set(idx, gId);
						gIt.remove();
						break;
					}
				}
			}

			if (g.getIsComplete()) {
				smASeed = smIdMap.get(g.getWinner().getId());
				promoteWinner(gameIds.indexOf(g.getId()), smASeed);
			}
		}
		return sGames;
	}

	private void promoteWinner(int idx, int wIdx) {
		assert gameIds.get(idx) != -1;
		boolean sm1Wins = true;

		if (wIdx == sm2Idcs.get(idx)) {
			sm1Wins = false;
		} else {
			assert wIdx == sm1Idcs.get(idx);
		}

		if (sm1Wins) {
			sm1Types.set(idx, BrNodeType.WIN);
			sm2Types.set(idx, BrNodeType.LOSS);
			wIdx = sm1Idcs.get(idx);
		} else {
			sm1Types.set(idx, BrNodeType.LOSS);
			sm2Types.set(idx, BrNodeType.WIN);
			wIdx = sm2Idcs.get(idx);
		}

		int childViewId = getChildViewId(matchIds.get(idx));
		int childIdx = matchIds.indexOf(childViewId % BrNodeType.MOD);

		if (isUpperView(childViewId)) {
			sm1Idcs.set(childIdx, wIdx);
			sm1Types.set(childIdx, BrNodeType.TIP);
		} else {
			sm2Idcs.set(childIdx, wIdx);
			sm2Types.set(childIdx, BrNodeType.TIP);
		}
	}

	private Boolean hasSm(int idx, int smIdx) {
		if (sm1Idcs.get(idx) == smIdx || sm2Idcs.get(idx) == smIdx) {
			return true;
		} else {
			return false;
		}
	}

	public MatchInfo getMatchInfo(int viewId) {
		MatchInfo mInfo = new MatchInfo();
		int matchId = viewId % BrNodeType.MOD;
		if (matchIds.contains(matchId)) {
			int idx = matchIds.indexOf(matchId);
			mInfo = new MatchInfo(this, idx);
		}
		return mInfo;
	}

	public int length() {
		assert matchIds.size() == sm1Idcs.size();
		assert matchIds.size() == sm1Types.size();
		assert matchIds.size() == sm2Idcs.size();
		assert matchIds.size() == sm2Types.size();
		assert matchIds.size() == gameIds.size();
		return matchIds.size();
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

		MatchInfo(Bracket bd, int idx) {
			gameId = bd.gameIds.get(idx);
			int sm1Type = bd.sm1Types.get(idx);
			int sm2Type = bd.sm2Types.get(idx);

			if (sm1Type == BrNodeType.TIP || sm1Type == BrNodeType.WIN
					|| sm1Type == BrNodeType.LOSS) {
				p1Id = smSeedMap.get(bd.sm1Idcs.get(idx)).getPlayer().getId();
			}

			if (sm2Type == BrNodeType.TIP || sm2Type == BrNodeType.WIN
					|| sm2Type == BrNodeType.LOSS) {
				p2Id = smSeedMap.get(bd.sm2Idcs.get(idx)).getPlayer().getId();
			}

			if (sm1Type == BrNodeType.TIP && sm2Type == BrNodeType.TIP) {
				allowCreate = true;
			}

			if (sm1Type == BrNodeType.WIN || sm1Type == BrNodeType.LOSS) {
				assert sm2Type == BrNodeType.WIN || sm2Type == BrNodeType.LOSS;
				allowView = true;
			}

			marquee += "[ " + bd.matchIds.get(idx) + " / " + gameId + " ] ";

			// upper player
			if (sm1Type == BrNodeType.UNSET) {
				marquee += "Unknown";
			} else {
				marquee += smSeedMap.get(bd.sm1Idcs.get(idx)).getPlayer()
						.getNickName();
				if (sm1Type == BrNodeType.WIN) {
					marquee += " (W)";
				} else if (sm1Type == BrNodeType.LOSS) {
					marquee += " (L)";
				}
			}

			// lower player
			if (sm2Type == BrNodeType.UNSET) {
				marquee += " -vs- Unknown";
			} else if (sm2Type == BrNodeType.NA) {
				marquee += ", tournament winner.";
			} else {
				marquee += " -vs- "
						+ smSeedMap.get(bd.sm2Idcs.get(idx)).getPlayer()
								.getNickName();
				if (sm2Type == BrNodeType.WIN) {
					marquee += " (W)";
				} else if (sm2Type == BrNodeType.LOSS) {
					marquee += " (L)";
				}
			}
		}
	}

	/** find n such that 2**n >= p */
	public static Integer factorTwos(int p) {
		Integer n = 1;
		while (Math.pow(2, n) < p) {
			n++;
		}
		double np = Math.pow(2, n);
		return n;
	}

	private int powerSeries(int maxPow, boolean evens) {
		int sum = 0;
		int startIdx = 1;
		if (evens) {
			startIdx = 0;
		}
		for (int ii = startIdx; ii <= maxPow; ii = ii + 2) {
			sum += Math.pow(2, ii);
		}
		Log.i("PowerSeries", "evens: " + evens + ", maxPow: " + maxPow
				+ ", sum: " + sum);
		return sum;
	}
}
