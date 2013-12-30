package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Game;
import com.ultimatepolish.db.OrmLiteFragment;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.db.Session;
import com.ultimatepolish.enums.SessionType;
import com.ultimatepolish.polishscorebook.backend.ListAdapter_Game;
import com.ultimatepolish.polishscorebook.backend.NavigationInterface;
import com.ultimatepolish.polishscorebook.backend.ViewHolderHeader_Game;
import com.ultimatepolish.polishscorebook.backend.ViewHolder_Game;

public class View_Games extends OrmLiteFragment {
	private static final String LOGTAG = "View_Games";
	NavigationInterface mNav;

	private LinkedHashMap<String, ViewHolderHeader_Game> sHash = new LinkedHashMap<String, ViewHolderHeader_Game>();
	private List<ViewHolderHeader_Game> sessionList = new ArrayList<ViewHolderHeader_Game>();
	private ListAdapter_Game gameAdapter;
	private ExpandableListView elv;
	private Switch viewAllGames;
	private View rootView;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_view_listing, container,
				false);

		elv = (ExpandableListView) rootView.findViewById(R.id.dbListing);
		gameAdapter = new ListAdapter_Game(context, sessionList);
		elv.setAdapter(gameAdapter);
		elv.setOnChildClickListener(elvItemClicked);
		elv.setOnGroupClickListener(elvGroupClicked);
		elv.setOnItemLongClickListener(elvItemLongClicked);

		viewAllGames = new Switch(context);
		viewAllGames.setTextOff("Unfinished");
		viewAllGames.setTextOn("All");
		viewAllGames.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshGamesListing();
			}
		});

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 20, 0, 20);
		// lp.addRule(RelativeLayout.BELOW, R.id.dbListing);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		((RelativeLayout) rootView).addView(viewAllGames, lp);
		rootView.requestLayout();
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = getActivity();

		try {
			mNav = (NavigationInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement NavigationInterface");
		}
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem fav = menu.add("New Game");
		fav.setIcon(R.drawable.ic_menu_add);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		fav.setIntent(new Intent(context, NewGame.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshGamesListing();
	}

	private void expandAll() {
		// method to expand all groups
		int count = gameAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.expandGroup(i);
		}
	}

	private void collapseAll() {
		// method to collapse all groups
		int count = gameAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.collapseGroup(i);
		}
	}

	protected void refreshGamesListing() {
		sHash.clear();
		sessionList.clear();
		// add all the sessions to the headers

		Session s;
		Player[] p = new Player[2];

		try {
			Dao<Session, Long> sessionDao = Session.getDao(context);
			Dao<Game, Long> gameDao = Game.getDao(context);
			Dao<Player, Long> playerDao = Player.getDao(context);

			for (Session sess : sessionDao) {
				if (sess.sessionType != SessionType.SNGL_ELIM
						&& sess.sessionType != SessionType.DBL_ELIM) {
					addSession(sess.getSessionName());
				}
			}

			// get all games or just incomplete based on switch
			List<Game> gamesList = new ArrayList<Game>();
			if (viewAllGames.isChecked()) {
				gamesList = gameDao.queryForAll();
			} else {
				try {
					gamesList = gameDao.queryBuilder()
							.orderBy(Game.DATE_PLAYED, true).where()
							.eq(Game.IS_COMPLETE, false).query();
				} catch (SQLException e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
			}

			for (Game g : gamesList) {
				sessionDao.refresh(g.getSession());
				s = g.getSession();

				if (s.sessionType != SessionType.SNGL_ELIM
						&& s.sessionType != SessionType.DBL_ELIM) {
					p[0] = playerDao.queryForId(g.getFirstPlayer().getId());
					p[1] = playerDao.queryForId(g.getSecondPlayer().getId());

					addGame(s.getSessionName(), String.valueOf(g.getId()),
							p[0].getNickName(), p[1].getNickName(),
							String.valueOf(g.getFirstPlayerScore()) + " / "
									+ String.valueOf(g.getSecondPlayerScore()));
				}
			}
		} catch (SQLException e) {
			loge("Retrieval of games/sessions failed", e);
		}
		expandAll();
		gameAdapter.notifyDataSetChanged(); // required if list has changed
	}

	private OnChildClickListener elvItemClicked = new OnChildClickListener() {
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			// get the group header
			ViewHolderHeader_Game sessionInfo = sessionList.get(groupPosition);
			// get the child info
			ViewHolder_Game gameInfo = sessionInfo.getGameList().get(
					childPosition);
			// display it or do something with it
			Toast.makeText(
					context,
					"Selected " + sessionInfo.getName() + "/"
							+ String.valueOf(gameInfo.getId()),
					Toast.LENGTH_SHORT).show();

			// load the game in progress screen
			Long gId = Long.valueOf(gameInfo.getId());
			mNav.loadGame(gId);
			return true;
		}
	};
	private OnItemLongClickListener elvItemLongClicked = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				int groupPosition = ExpandableListView
						.getPackedPositionGroup(id);
				int childPosition = ExpandableListView
						.getPackedPositionChild(id);

				// get the group header
				ViewHolderHeader_Game sessionInfo = sessionList
						.get(groupPosition);
				// get the child info
				ViewHolder_Game gameInfo = sessionInfo.getGameList().get(
						childPosition);
				// display it or do something with it
				Toast.makeText(
						context,
						"Selected " + sessionInfo.getName() + "/"
								+ String.valueOf(gameInfo.getId()),
						Toast.LENGTH_SHORT).show();

				// load the game in progress screen
				Long gid = Long.valueOf(gameInfo.getId());
				Intent intent = new Intent(context, Detail_Game.class);
				intent.putExtra("GID", gid);
				startActivity(intent);
				return true;
			}
			return false;
		}
	};
	private OnGroupClickListener elvGroupClicked = new OnGroupClickListener() {
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {

			// ViewHolderHeader_Game sessionInfo =
			// sessionList.get(groupPosition);
			// Toast.makeText(context, "Tapped " + sessionInfo.getName(),
			// Toast.LENGTH_SHORT).show();
			return false;
		}
	};

	private void addSession(String sessionName) {
		ViewHolderHeader_Game vhh_Game = new ViewHolderHeader_Game();
		vhh_Game.setName(sessionName);
		sessionList.add(vhh_Game);
		sHash.put(sessionName, vhh_Game);
	}

	private void addGame(String sort, String gameId, String p1, String p2,
			String score) {
		logd("addGame() - adding game " + gameId);
		// find the index of the session header
		ViewHolderHeader_Game sessionInfo = sHash.get(sort);
		List<ViewHolder_Game> gameList = sessionInfo.getGameList();

		// create a new child and add that to the group
		ViewHolder_Game gameInfo = new ViewHolder_Game();
		gameInfo.setId(gameId);
		gameInfo.setPlayerOne(p1);
		gameInfo.setPlayerTwo(p2);
		gameInfo.setScore(score);
		gameList.add(gameInfo);
		sessionInfo.setGameList(gameList);
	}
}
