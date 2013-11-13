package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

public class ActiveGame {
	private Context context;
	private int activeIdx;

	private Game g;
	private Player[] p = new Player[2];
	private Session s;
	private ArrayList<Throw> tArray;
	private Venue v;

	private Dao<Game, Long> gDao;
	private Dao<Player, Long> pDao;
	private Dao<Session, Long> sDao;
	private Dao<Throw, Long> tDao;
	private Dao<Venue, Long> vDao;

	public ActiveGame(Game g, Context context) {
		super();
		this.g = g;

		gDao = Game.getDao(context);
		pDao = Player.getDao(context);
		sDao = Session.getDao(context);
		tDao = Throw.getDao(context);
		vDao = Venue.getDao(context);

		try {
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

	void updateScoresFrom(int idx) {
		Throw t, u;
		for (int i = idx; i < nThrows(); i++) {
			t = getThrow(i);
			if (i == 0) {
				t.setInitialScores();
				t.setOffenseFireCount(0);
				t.setDefenseFireCount(0);
			} else {
				u = getPreviousThrow(t);
				t.setInitialScores(u);
				t.setFireCounts(u);
			}
		}
		updateGameScore();
	}

	private void updateGameScore() {
		int[] scores = { 0, 0 };
		if (nThrows() > 0) {
			Throw lastThrow = getThrow(nThrows() - 1);
			if (Throw.isP1Throw(lastThrow)) {
				scores = lastThrow.getFinalScores();
			} else {
				int[] tmp = lastThrow.getFinalScores();
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
			t.setThrowIdx(idx);
			assert g.isValidThrow(t) : "invalid throw for index " + idx;
			t = tArray.set(idx, t);
			saveThrow(t);
		} else if (idx == nThrows()) {
			t.setThrowIdx(idx);
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
				t.setInitialScores();
			} else {
				Throw u = getPreviousThrow(t);
				t.setInitialScores(u);
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
		int idx = t.getThrowIdx();
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
		HashMap<String, Object> m = t.getQueryMap();
		List<Throw> tList = new ArrayList<Throw>();
		try {
			tList = tDao.queryForFieldValuesArgs(m);
		} catch (SQLException e) {
			throw new RuntimeException("could not query for throw "
					+ t.getThrowIdx() + ", game " + t.getGame().getId());
		}
		try {
			if (tList.isEmpty()) {
				assert g.isValidThrow(t) : "invalid throw for index "
						+ t.getThrowIdx() + ", not saving";
				tDao.create(t);
			} else {
				assert g.isValidThrow(t) : "invalid throw for index "
						+ t.getThrowIdx() + ", not updating";
				t.setId(tList.get(0).getId());
				tDao.update(t);
			}
		} catch (SQLException e) {
			throw new RuntimeException("could not create/update throw "
					+ t.getThrowIdx() + ", game " + t.getGame().getId());
		}
	}

	public void saveAllThrows() {
		updateScoresFrom(0);
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

	public void saveGame() {
		try {
			gDao.update(g);
		} catch (SQLException e) {
			throw new RuntimeException("could not save game " + g.getId());
		}
	}

}
