package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.ultimatepolish.db.SessionMember;

public class Bracket {
	public static String LOGTAG = "Bracket";

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

	public Bracket(int nLeafs) {
		this.nLeafs = (int) Math.pow(2, factorTwos(nLeafs));
		addMatches();
	}

	private void addMatches() {
		for (int ii = 0; ii < nLeafs; ii++) {
			matchIds.add(ii);
			sm1Idcs.add(-1);
			sm1Types.add(BrNodeType.UNSET);
			sm2Idcs.add(-1);
			sm2Types.add(BrNodeType.UNSET);
			gameIds.add((long) -1);
		}
		sm2Types.set(nLeafs - 1, BrNodeType.NA);
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
		int matchId = viewId % BrNodeType.MOD;
		return ((Double) Math.floor(-Math.log(1 - ((double) matchId) / nLeafs)
				/ Math.log(2))).intValue();
	}

	public int getTopMatchOfTier(int tier) {
		return (int) (nLeafs * (1 - Math.pow(2, -tier)));
	}

	public int getTopParentMatch(int bracketIdx) {
		// can take bracket idx or match idx
		Integer matchIdx = bracketIdx % BrNodeType.MOD;
		Integer tier = getTier(matchIdx);
		Integer topOfTier = getTopMatchOfTier(tier);
		Integer topOfPrevTier = getTopMatchOfTier(tier - 1);

		Integer topParentMatch = topOfPrevTier + 2 * (matchIdx - topOfTier);
		return topParentMatch;
	}

	public int getChildBracketId(int bracketIdx) {
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
		Log.i(LOGTAG, "findviewabove, headerOffset is " + headerIdOffset);

		if (!isUpperView(viewId)) {
			viewAboveId = matchId + matchIdOffset + BrNodeType.UPPER;
		} else {
			Integer baseId = matchId;
			if (getTier(matchId) > 0) {
				baseId = getTopParentMatch(matchId);
			}
			if (matchIds.contains(baseId) && baseId != matchId) {
				viewAboveId = baseId + matchIdOffset + BrNodeType.UPPER;
			} else {
				if (baseId > getTopMatchOfTier(getTier(baseId))) {
					// have to keep track of upper/lower now
					baseId += BrNodeType.LOWER - 1; // lower arm of the match
												// above
					while (!matchIds.contains(baseId % BrNodeType.MOD)) {
						baseId = getChildBracketId(baseId);
					}
					viewAboveId = baseId + matchIdOffset;
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

	public void seed(List<SessionMember> sMembers) {
		// seed the winners bracket
		for (Integer ii = 0; ii < sMembers.size(); ii += 2) {
			if (sMembers.get(ii + 1).getSeed() == BrNodeType.BYE) {
				modSMs(ii / 2, ii, BrNodeType.TIP, ii + 1, BrNodeType.BYE);
			} else {
				modSMs(ii / 2, ii, BrNodeType.TIP, ii + 1, BrNodeType.TIP);
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
			if (sm2Types.get(ii) == BrNodeType.BYE) {
				childViewId = getChildBracketId(matchIds.get(ii));
				childMatchId = childViewId % BrNodeType.MOD;
				assert matchIds.indexOf(childMatchId) == childMatchId;

				if (isUpperView(childViewId)) {
					sm1Idcs.set(childMatchId, sm1Idcs.get(ii));
					sm1Types.set(childMatchId, sm1Types.get(ii));
				} else {
					sm2Idcs.set(childMatchId, sm1Idcs.get(ii));
					sm2Types.set(childMatchId, sm1Types.get(ii));
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
			sm1Types.set(idx, BrNodeType.WIN);
			sm2Types.set(idx, BrNodeType.LOSS);
			wIdx = sm1Idcs.get(idx);
		} else {
			sm1Types.set(idx, BrNodeType.LOSS);
			sm2Types.set(idx, BrNodeType.WIN);
			wIdx = sm2Idcs.get(idx);
		}

		int childViewId = getChildBracketId(matchIds.get(idx));
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

	/** find n such that 2**n >= p */
	public Integer factorTwos(int p) {
		Integer n = 1;
		while (Math.pow(2, n) < p) {
			n++;
		}
		return n;
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
				p1Id = sMembers.get(bd.sm1Idcs.get(idx)).getPlayer().getId();
			}

			if (sm2Type == BrNodeType.TIP || sm2Type == BrNodeType.WIN
					|| sm2Type == BrNodeType.LOSS) {
				p2Id = sMembers.get(bd.sm2Idcs.get(idx)).getPlayer().getId();
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
				marquee += sMembers.get(bd.sm1Idcs.get(idx)).getPlayer()
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
						+ sMembers.get(bd.sm2Idcs.get(idx)).getPlayer()
								.getNickName();
				if (sm2Type == BrNodeType.WIN) {
					marquee += " (W)";
				} else if (sm2Type == BrNodeType.LOSS) {
					marquee += " (L)";
				}
			}
		}
	}

	private final class BrNodeType {
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
