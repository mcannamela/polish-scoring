package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

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
	private Context context;
	private Session s;
	private List<SessionMember> sMembers = new ArrayList<SessionMember>();
	private Boolean isDoubleElim;
	public RelativeLayout rl;

	// bracketMap maps a member to the appropriate view id
	public BidiMap<SessionMember, Integer> bracketMap = new TreeBidiMap<SessionMember, Integer>();
	// sMemberMap maps a player id to a session member
	public BidiMap<Long, SessionMember> sMemberMap = new TreeBidiMap<Long, SessionMember>();

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

		createSingleElimBracket();
	}

	private void createSingleElimBracket() {
		// matches and tiers are 0 based.
		// matches are numbered top to bottom starting at tier 0 and continuing
		// in higher tiers
		// the upper bracket of a match is given the id = 1000 + matchId
		// similarly, the lower bracket is given the id = 2000 + matchId

		rl = new RelativeLayout(context);
		this.context = rl.getContext();
		RelativeLayout.LayoutParams lp;
		TextView tv;
		Integer matchIdx;
		SessionMember dummySessionMember = new SessionMember();
		dummySessionMember.setPlayerSeed(-1);
		dummySessionMember.setPlayerRank(-1000);

		foldRoster();
		makeInvisibleHeaders(rl);

		// create the lowest tier
		for (Integer i = 0; i < sMembers.size() - 1; i += 2) {
			Log.i("SessionDetails",
					"Match idx " + String.valueOf(i / 2) + ", "
							+ sMembers.get(i).getPlayerSeed() + " vs "
							+ sMembers.get(i + 1).getPlayerSeed());
			matchIdx = i / 2;

			// populate the bracket map

			if (sMembers.get(i + 1).getPlayerSeed() == -1) {
				sMembers.get(i).setPlayerRank(1);
				bracketMap.put(sMembers.get(i), getChildBracketId(matchIdx));
				tv = makeHalfBracket(context, dummySessionMember, true, false);
				tv.setBackgroundResource(0);
				tv.setText(null);
				tv.setId(matchIdx + 1000);
				addViewToLayout(tv);
				tv = makeHalfBracket(context, dummySessionMember, true, true);
				tv.setBackgroundResource(0);
				tv.setText(null);
				tv.setId(matchIdx + 2000);
				addViewToLayout(tv);
			} else {
				bracketMap.put(sMembers.get(i), matchIdx + 1000);
				bracketMap.put(sMembers.get(i + 1), matchIdx + 2000);

				// upper half of match bracket
				tv = makeHalfBracket(context, sMembers.get(i), true, true);
				tv.setId(matchIdx + 1000);
				addViewToLayout(tv);

				// lower half of match bracket
				tv = makeHalfBracket(context, sMembers.get(i + 1), false, true);
				tv.setId(matchIdx + 2000);
				addViewToLayout(tv);
			}
		}

		// create higher tiers
		dummySessionMember.setPlayerSeed(-2);
		for (Integer i = sMembers.size() / 2; i < sMembers.size() - 1; i++) {
			matchIdx = i;

			// upper half of match bracket
			if (bracketMap.containsValue(matchIdx + 1000)) {
				SessionMember sMember = bracketMap.inverseBidiMap().get(
						matchIdx + 1000);

				tv = makeHalfBracket(context, sMember, true, true);
				tv.setId(matchIdx + 1000);
				lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				lp.addRule(RelativeLayout.ALIGN_RIGHT, getTier(matchIdx) + 1);
				lp.addRule(RelativeLayout.ALIGN_BOTTOM,
						getTopParentMatch(matchIdx) + 1000);
				if (matchIdx != 0) {
					lp.setMargins(0, -2, 0, 0);
				}
				rl.addView(tv, lp);
			} else {
				tv = makeHalfBracket(context, dummySessionMember, true, false);
				tv.setId(matchIdx + 1000);
				addViewToLayout(tv);
			}

			// lower half of match bracket
			if (bracketMap.containsValue(matchIdx + 2000)) {
				SessionMember sMember = bracketMap.inverseBidiMap().get(
						matchIdx + 2000);
				tv = makeHalfBracket(context, sMember, false, true);
				tv.setId(matchIdx + 2000);
				lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				lp.addRule(RelativeLayout.ALIGN_RIGHT, getTier(matchIdx) + 1);
				lp.addRule(RelativeLayout.BELOW, matchIdx + 1000);
				lp.setMargins(0, 0, 0, -2);

				rl.addView(tv, lp);
			} else {
				tv = makeHalfBracket(context, dummySessionMember, false, false);
				tv.setId(matchIdx + 2000);
				addViewToLayout(tv);
			}
		}

		// create winner view
		tv = new TextView(context);
		tv.setId(sMembers.size() - 1 + 1000);
		tv.setBackgroundResource(R.drawable.bracket_endpoint);
		tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
		addViewToLayout(tv);
	}

	public void refreshSingleElimBracket() {
		TextView tv;
		Integer matchIdx;
		RelativeLayout.LayoutParams lp;

		restartBracketMap();

		// get all the completed games for the session, ordered by date played
		List<Game> sGamesList = new ArrayList<Game>();
		try {
			Log.i("bracket", "session id is " + s.getId());
			sGamesList = gDao.queryBuilder().orderBy(Game.DATE_PLAYED, true)
					.where().eq(Game.SESSION, s.getId()).and()
					.eq(Game.IS_COMPLETE, true).query();
			for (Game g : sGamesList) {
				gDao.refresh(g);
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		// step through the games, promoting and eliminating players
		SessionMember winner;
		SessionMember loser;
		Integer newWinnerBracket;

		for (Game g : sGamesList) {
			winner = sMemberMap.get(g.getWinner().getId());
			loser = sMemberMap.get(g.getLoser().getId());
			Log.i("bracket", "winner id is " + g.getWinner().getId()
					+ ", loser id is " + g.getLoser().getId());

			newWinnerBracket = getChildBracketId(bracketMap.get(winner));
			Log.i("bracket", "winner bracket id is " + newWinnerBracket);
			tv = (TextView) rl.findViewById(newWinnerBracket);
			tv.getBackground().setColorFilter(winner.getPlayer().color,
					Mode.MULTIPLY);
			bracketMap.remove(winner);
			bracketMap.put(winner, newWinnerBracket);
			sMembers.get(sMembers.indexOf(winner)).setPlayerRank(
					getTier(newWinnerBracket));

			tv = (TextView) rl.findViewById(bracketMap.get(loser));
			if (tv.getText() != "") {
				if (bracketMap.get(loser) >= 2000) {
					tv.setBackgroundResource(R.drawable.bracket_bottom_eliminated_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_top_eliminated_labeled);
				}
			} else {
				if (bracketMap.get(loser) >= 2000) {
					tv.setBackgroundResource(R.drawable.bracket_bottom_eliminated);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_top_eliminated);
				}
			}
			tv.getBackground().setColorFilter(loser.getPlayer().color,
					Mode.MULTIPLY);
		}
	}

	public void restartBracketMap() {
		bracketMap.clear();
		Integer matchIdx;

		for (Integer i = 0; i < sMembers.size() - 1; i += 2) {
			Log.i("SessionDetails",
					"Match idx " + String.valueOf(i / 2) + ", "
							+ sMembers.get(i).getPlayerSeed() + " vs "
							+ sMembers.get(i + 1).getPlayerSeed());
			matchIdx = i / 2;

			// populate the bracket map
			bracketMap.put(sMembers.get(i), matchIdx + 1000);
			if (sMembers.get(i + 1).getPlayerSeed() >= 0) {
				bracketMap.put(sMembers.get(i + 1), matchIdx + 2000);
			} else if (sMembers.get(i + 1).getPlayerSeed() == -1) {
				sMembers.get(i).setPlayerRank(1);
				bracketMap.remove(sMembers.get(i));
				bracketMap.put(sMembers.get(i), getChildBracketId(matchIdx));
			}
		}
	}

	public void foldRoster() {
		// expand the list size to the next power of two
		Integer n = factorTwos(sMembers.size());

		SessionMember dummySessionMember = new SessionMember();
		dummySessionMember.setPlayerSeed(-1);
		dummySessionMember.setPlayerRank(-1000);

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

	public TextView makeHalfBracket(Context context, SessionMember member,
			Boolean onTop, Boolean addLabels) {
		TextView tv = new TextView(context);

		Boolean isBye = member.getPlayerSeed() == -1;
		Boolean isUnset = member.getPlayerSeed() == -2;

		if (addLabels) {
			if (isBye) {
				tv.setHeight(10);
			} else {
				tv.setText("(" + String.valueOf(member.getPlayerSeed() + 1)
						+ ") " + member.getPlayer().getNickName());
				if (onTop) {
					tv.setBackgroundResource(R.drawable.bracket_top_labeled);
				} else {
					tv.setBackgroundResource(R.drawable.bracket_bottom_labeled);
				}
			}
		} else if (!isBye) {
			if (onTop) {
				tv.setBackgroundResource(R.drawable.bracket_top);
			} else {
				tv.setBackgroundResource(R.drawable.bracket_bottom);
			}
		}

		tv.setGravity(Gravity.RIGHT);
		tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
		if (isUnset) {
			// in this case, its actually an unset game
			tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
		} else if (!isBye) {
			tv.getBackground().setColorFilter(member.getPlayer().getColor(),
					Mode.MULTIPLY);
		}

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

	private void addViewToLayout(TextView tv) {
		Integer matchIdx = tv.getId() % 1000;
		Boolean onTop = tv.getId() < 2000;
		Integer tier = getTier(matchIdx);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_LEFT, tier + 1);
		lp.addRule(RelativeLayout.ALIGN_RIGHT, tier + 1);

		if (tier == 0) {
			// lp.addRule(RelativeLayout.ALIGN_RIGHT, 1);
			if (onTop) {
				if (matchIdx != 0) {
					lp.addRule(RelativeLayout.BELOW, matchIdx - 1 + 2000);
				}
				lp.setMargins(0, 8, 0, 0);
			} else {
				lp.addRule(RelativeLayout.BELOW, matchIdx + 1000);
				lp.setMargins(0, 0, 0, 8);
			}
		} else if (tier == getTier(sMembers.size() - 1)) {
			Integer topParent = getTopParentMatch(matchIdx);
			lp.addRule(RelativeLayout.ALIGN_BOTTOM, topParent + 1000);
			lp.setMargins(0, 0, 0, -19);
		} else {
			if (onTop) {
				Integer topParentMatch = getTopParentMatch(matchIdx);
				lp.addRule(RelativeLayout.ALIGN_BOTTOM, topParentMatch + 2000);
				lp.addRule(RelativeLayout.BELOW, topParentMatch + 1000);
				lp.setMargins(0, -2, 0, 0);
			} else {
				Integer bottomParentMatch = getTopParentMatch(matchIdx) + 1;
				lp.addRule(RelativeLayout.ABOVE, bottomParentMatch + 2000);
				lp.addRule(RelativeLayout.BELOW, matchIdx + 1000);
				lp.setMargins(0, 0, 0, -2);
			}
		}
		rl.addView(tv, lp);
	}

	@Override
	public void onClick(View v) {
		Log.i("Bracket", "howdy, game " + v.getId());
	}
}
