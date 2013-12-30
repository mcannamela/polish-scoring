package com.ultimatepolish.polishscorebook.backend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;
import android.graphics.Color;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Game;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.db.Session;
import com.ultimatepolish.db.Throw;
import com.ultimatepolish.db.Venue;
import com.ultimatepolish.enums.RuleType;
import com.ultimatepolish.enums.SessionType;
import com.ultimatepolish.rulesets.RuleSet;

public class ActiveGame {
	private Context context;
	private int activeIdx;

	private long gId;
	private Game g;
	private Player[] p = new Player[2];
	private Session s;
	private ArrayList<Throw> tArray;
	private Venue v;
	public RuleSet ruleSet;

	private Dao<Game, Long> gDao;
	private Dao<Player, Long> pDao;
	private Dao<Session, Long> sDao;
	private Dao<Throw, Long> tDao;
	private Dao<Venue, Long> vDao;

	public ActiveGame(long gId, Context context, int testRuleSetId) {
		super();

		this.gId = gId;
		gDao = Game.getDao(context);
		pDao = Player.getDao(context);
		sDao = Session.getDao(context);
		tDao = Throw.getDao(context);
		vDao = Venue.getDao(context);

		if (gId != -1) {
			try {
				g = gDao.queryForId(gId);
				sDao.refresh(g.getSession());
				vDao.refresh(g.getVenue());
				pDao.refresh(g.getFirstPlayer());
				pDao.refresh(g.getSecondPlayer());

				tArray = g.getThrowList(context);
			} catch (SQLException e) {
				throw new RuntimeException("couldn't get throws for game "
						+ g.getId() + ": ", e);
			}

			s = g.getSession();
			v = g.getVenue();
			p[0] = g.getFirstPlayer();
			p[1] = g.getSecondPlayer();
			ruleSet = RuleType.map.get(g.ruleSetId);

		} else {
			// if no game ID is passed in, this is for testing (or an error)
			// so create dummy objects which won't be saved to database
			s = new Session("DummySession", SessionType.LEAGUE,
					RuleType.rsNull, new Date(), false);
			v = new Venue("DummyVenue", true);
			p[0] = new Player("Dum", "Dumb", "P1", false, true, false, true,
					15, 70, new byte[0], Color.RED);
			p[1] = new Player("Bum", "Bumb", "P2", false, true, false, true,
					15, 70, new byte[0], Color.BLUE);
			g = new Game(p[0], p[1], s, v, RuleType.rs00, true, new Date());
			ruleSet = RuleType.map.get(testRuleSetId);
			tArray = new ArrayList<Throw>();
		}

		activeIdx = 0;
		if (tArray.size() > 0) {
			activeIdx = tArray.size() - 1;
		}
		updateScoresFrom(0);
	}

	private Throw makeNextThrow() {
		Throw t = g.makeNewThrow(nThrows());
		return t;
	}

	private void setInitialScores(Throw t, Throw previousThrow) {
		int[] scores = ruleSet.getFinalScores(previousThrow);
		t.initialDefensivePlayerScore = scores[0];
		t.initialOffensivePlayerScore = scores[1];
	}

	private void setInitialScores(Throw t) {
		t.initialDefensivePlayerScore = 0;
		t.initialOffensivePlayerScore = 0;
	}

	public void updateScoresFrom(int idx) {
		Throw t, u;
		for (int i = idx; i < nThrows(); i++) {
			t = getThrow(i);
			if (i == 0) {
				setInitialScores(t);
				t.offenseFireCount = 0;
				t.defenseFireCount = 0;
			} else {
				u = getPreviousThrow(t);
				setInitialScores(t, u);
				ruleSet.setFireCounts(t, u);
			}
		}
		updateGameScore();
	}

	private void updateGameScore() {
		int[] scores = { 0, 0 };
		if (nThrows() > 0) {
			Throw lastThrow = getThrow(nThrows() - 1);
			if (Throw.isP1Throw(lastThrow)) {
				scores = ruleSet.getFinalScores(lastThrow);
			} else {
				int[] tmp = ruleSet.getFinalScores(lastThrow);
				scores[1] = tmp[0];
				scores[0] = tmp[1];
			}
		}
		g.setFirstPlayerScore(scores[0]);
		g.setSecondPlayerScore(scores[1]);
	}

	private ArrayList<Long> getThrowIds() {
		HashMap<String, Object> m;
		List<Throw> tList = new ArrayList<Throw>();
		ArrayList<Long> throwIds = new ArrayList<Long>();
		int cnt = 0;
		try {
			for (Throw t : tArray) {
				m = t.getQueryMap();
				tList = tDao.queryForFieldValuesArgs(m);
				if (tList.isEmpty()) {
					throwIds.add(Long.valueOf(-1));
				} else {
					throwIds.add(tList.get(0).getId());
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("could not query for throw ids");
		}
		return throwIds;
	}

	public Throw getActiveThrow() {
		return getThrow(activeIdx);
	}

	public void updateActiveThrow(Throw t) {
		setThrow(activeIdx, t);
	}

	public void setThrow(int idx, Throw t) {
		if (idx < 0) {
			throw new RuntimeException("must have positive throw index, not: "
					+ idx);
		} else if (idx >= 0 && idx < nThrows()) {
			t.throwIdx = idx;
			assert g.isValidThrow(t) : "invalid throw for index " + idx;
			t = tArray.set(idx, t);
			saveThrow(t);
		} else if (idx == nThrows()) {
			t.throwIdx = idx;
			assert g.isValidThrow(t) : "invalid throw for index " + idx;
			tArray.add(t);
			saveThrow(t);
		} else if (idx > nThrows()) {
			throw new RuntimeException("cannot set throw " + idx
					+ " in the far future");
		}
		updateScoresFrom(idx);
	}

	public Throw getThrow(int idx) {
		Throw t = null;
		if (idx < 0) {
			throw new RuntimeException("must have positive throw index, not: "
					+ idx);
		} else if (idx >= 0 && idx < nThrows()) {
			t = tArray.get(idx);
		} else if (idx == nThrows()) {
			t = makeNextThrow();
			if (idx == 0) {
				setInitialScores(t);
			} else {
				Throw u = getPreviousThrow(t);
				setInitialScores(t, u);
			}

			tArray.add(t);
		} else if (idx > nThrows()) {
			throw new RuntimeException("cannot get throw " + idx
					+ " from the far future");
		}
		if (t == null) {
			throw new NullPointerException("Got invalid throw for index " + idx);
		}
		return t;
	}

	public Throw getPreviousThrow(Throw t) {
		Throw u = null;
		int idx = t.throwIdx;
		if (idx <= 0) {
			throw new RuntimeException("throw " + idx + " has no predecessor");
		} else if (idx > 0 && idx <= nThrows()) {
			u = tArray.get(idx - 1);
		} else if (idx > nThrows()) {
			throw new RuntimeException("cannot get predecessor of throw " + idx
					+ " from the far future");
		}
		if (u == null) {
			throw new NullPointerException(
					"Got invalid predecessor for throw index " + idx);
		}
		return u;
	}

	public int nThrows() {
		return tArray.size();
	}

	public ArrayList<Throw> getThrows() {
		return tArray;
	}

	public int getActiveIdx() {
		return activeIdx;
	}

	public void setActiveIdx(int activeIdx) {
		this.activeIdx = activeIdx;
	}

	public Game getGame() {
		return g;
	}

	public void setGame(Game g) {
		this.g = g;
	}

	public long getGameId() {
		return g.getId();
	}

	public Date getGameDate() {
		return g.getDatePlayed();
	}

	public String getP1Name() {
		return p[0].getDisplayName();
	}

	public String getP2Name() {
		return p[1].getDisplayName();
	}

	public String getP1Nick() {
		return p[0].getNickName();
	}

	public String getP2Nick() {
		return p[1].getNickName();
	}

	public String getSessionName() {
		return s.getSessionName();
	}

	public String getVenueName() {
		return v.getName();
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	/* Saving functions */
	private void saveThrow(Throw t) {
		if (gId != -1) {
			HashMap<String, Object> m = t.getQueryMap();
			List<Throw> tList = new ArrayList<Throw>();
			try {
				tList = tDao.queryForFieldValuesArgs(m);
			} catch (SQLException e) {
				throw new RuntimeException("could not query for throw "
						+ t.throwIdx + ", game " + t.getGame().getId());
			}
			try {
				if (tList.isEmpty()) {
					assert g.isValidThrow(t) : "invalid throw for index "
							+ t.throwIdx + ", not saving";
					tDao.create(t);
				} else {
					assert g.isValidThrow(t) : "invalid throw for index "
							+ t.throwIdx + ", not updating";
					t.setId(tList.get(0).getId());
					tDao.update(t);
				}
			} catch (SQLException e) {
				throw new RuntimeException("could not create/update throw "
						+ t.throwIdx + ", game " + t.getGame().getId());
			}
		}
	}

	public void saveAllThrows() {
		updateScoresFrom(0);
		if (gId != -1) {
			final ArrayList<Long> throwIds = getThrowIds();
			try {
				tDao.callBatchTasks(new Callable<Void>() {
					public Void call() throws SQLException {
						long id;
						Throw t;
						for (int i = 0; i < tArray.size(); i++) {
							id = throwIds.get(i);
							t = tArray.get(i);
							if (id == -1) {
								tDao.create(t);
							} else {
								t.setId(id);
								tDao.update(t);
							}
						}
						return null;
					}
				});
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	public void saveGame() {
		if (gId != -1) {
			try {
				gDao.update(g);
			} catch (SQLException e) {
				throw new RuntimeException("Could not save game " + g.getId());
			}
		}
	}
}
