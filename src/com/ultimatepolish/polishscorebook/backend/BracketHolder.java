package com.ultimatepolish.polishscorebook.backend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ultimatepolish.db.Game;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.db.Session;
import com.ultimatepolish.db.SessionMember;
import com.ultimatepolish.enums.BrNodeType;
import com.ultimatepolish.polishscorebook.backend.Bracket.MatchInfo;

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
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		rl = new RelativeLayout(context);
		this.context = rl.getContext();

		foldRoster();
		wBr = new Bracket(sMembers, rl);
		wBr.buildBracket(context, this);

		if (isDoubleElim) {
			lBr = new Bracket(sMembers.size(), rl);
			lBr.changeOffsets(Bracket.factorTwos(sMembers.size()) + 1, 0);
			lBr.seedFromParentBracket(wBr);
			lBr.buildBracket(context, 82, wBr.lowestViewId(), 1, this);
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

		sGamesList = wBr.matchMatches(sGamesList);
		wBr.refreshViews();

		if (isDoubleElim) {
			lBr.respawnFromParentBracket(wBr);
			sGamesList = lBr.matchMatches(sGamesList);
			lBr.refreshViews();
			// sGamesList = fBr.matchMatches(sGamesList);
			// fBr.refreshWinnersBracket();
		}
		assert sGamesList.isEmpty();
	}

	public void foldRoster() {
		// expand the list size to the next power of two
		Integer n = Bracket.factorTwos(sMembers.size());

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

	public MatchInfo getMatchInfo(int viewId) {
		MatchInfo mInfo = wBr.getMatchInfo(viewId);

		if (isDoubleElim) {
			if (lBr.hasView(viewId)) {
				mInfo = lBr.getMatchInfo(viewId);
			}
			// else if (fBr.hasView(viewId)) {
			// mInfo = fBr.getMatchInfo(viewId);
			// }
		}

		return mInfo;
	}

	@Override
	public void onClick(View v) {
		Log.i(LOGTAG, "View " + v.getId() + " was clicked");
	}
}
