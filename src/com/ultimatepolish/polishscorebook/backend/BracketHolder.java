package com.ultimatepolish.polishscorebook.backend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import com.ultimatepolish.enums.BrNodeType;

public class BracketHolder implements View.OnClickListener {
	public static String LOGTAG = "BracketHolder";
	public Context context;
	private Session s;
	public RelativeLayout rl;
	private List<SessionMember> sMembers = new ArrayList<SessionMember>();
	private Boolean isDoubleElim;
	private Bracket wBr; // winners bracket
	private Bracket lBr; // losers bracket
	private Bracket fBr; // finals bracket

	// sMemberMap maps a player id to a session member
	public HashMap<Long, SessionMember> sMemberMap = new HashMap<Long, SessionMember>();

	Dao<Session, Long> sDao;
	Dao<SessionMember, Long> smDao;
	Dao<Player, Long> pDao;
	Dao<Game, Long> gDao;

	public BracketHolder(ScrollView sv, Session s, Boolean isDoubleElim) {
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
		wBr = new Bracket(sMembers, rl);
		buildWinnersBracket();

		if (isDoubleElim) {

			lBr = new Bracket(sMembers, rl);
			lBr.changeOffsets(factorTwos(sMembers.size()) + 1, sMembers.size());
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

		// lay out the bracket
		makeInvisibleHeaders(350, 150, 0, 0, 0);

		for (Integer mPos = 0; mPos < wBr.length(); mPos++) {
			// upper half of match
			tv = wBr.makeHalfMatchView(context, mPos, true);
			tv.setOnClickListener(this);
			if (wBr.sm1Types.get(mPos) == BrNodeType.TIP) {
				wBr.addViewToLayout(tv, true);
			} else {
				wBr.addViewToLayout(tv, false);
			}

			// lower half of match
			if (wBr.sm2Types.get(mPos) != BrNodeType.NA) {
				tv = wBr.makeHalfMatchView(context, mPos, false);
				if (wBr.sm2Types.get(mPos) == BrNodeType.TIP) {
					wBr.addViewToLayout(tv, true);
				} else {
					wBr.addViewToLayout(tv, false);
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

		// lay out the bracket
		int wBrLowest;
		if (wBr.matchIds.contains(sMembers.size() / 2)) {
			wBrLowest = sMembers.size() / 2 - 1 + BrNodeType.LOWER;
		} else {
			wBrLowest = wBr.findViewAbove(sMembers.size() / 2 - 1
					+ BrNodeType.LOWER);
		}
		makeInvisibleHeaders(350, 150, 1, wBrLowest, 10);

		for (Integer mPos = 0; mPos < lBr.length(); mPos++) {
			// populate the bracket map

			// upper half of match
			tv = lBr.makeHalfMatchView(context, mPos, true);
			tv.setOnClickListener(this);
			if (lBr.sm1Types.get(mPos) == BrNodeType.TIP) {
				lBr.addViewToLayout(tv, true);
			} else {
				lBr.addViewToLayout(tv, false);
			}

			// lower half of match
			if (lBr.sm2Types.get(mPos) != BrNodeType.NA) {
				tv = lBr.makeHalfMatchView(context, mPos, false);
				tv.setOnClickListener(this);
				if (lBr.sm2Types.get(mPos) == BrNodeType.TIP) {
					lBr.addViewToLayout(tv, true);
				} else {
					lBr.addViewToLayout(tv, false);
				}
			}
		}
	}

	public void refreshBrackets() {
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
			smIdxA = sMemberMap.get(g.getFirstPlayer().getId()).getSeed();
			smIdxB = sMemberMap.get(g.getSecondPlayer().getId()).getSeed();
			wBr.matchMatches(g.getId(), smIdxA, smIdxB);

			if (g.getIsComplete()) {
				smIdxA = sMemberMap.get(g.getWinner().getId()).getSeed();
				wBr.promoteWinner(wBr.gameIds.indexOf(g.getId()), smIdxA);
			}
		}

		wBr.refresh();
		if (isDoubleElim) {
			lBr.refresh();
			// fBr.refreshWinnersBracket();
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

	public void foldRoster() {
		// expand the list size to the next power of two
		Integer n = factorTwos(sMembers.size());

		SessionMember dummySessionMember = new SessionMember(BrNodeType.BYE,
				-1000);

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

	@Override
	public void onClick(View v) {
		Log.i(LOGTAG, "View " + v.getId() + " was clicked");
	}

	public MatchInfo getMatchInfo(int viewId) {
		MatchInfo mInfo = new MatchInfo();
		int matchId = viewId % BrNodeType.MOD;
		if (wBr.matchIds.contains(matchId)) {
			int idx = wBr.matchIds.indexOf(matchId);
			mInfo = new MatchInfo(wBr, idx);
		}
		return mInfo;
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

	/** find n such that 2**n >= p */
	public Integer factorTwos(int p) {
		Integer n = 1;
		while (Math.pow(2, n) < p) {
			n++;
		}
		return n;
	}
}
