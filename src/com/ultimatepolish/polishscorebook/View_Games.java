package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;

public class View_Games extends MenuContainerActivity {
	private static final String LOGTAG = "View_Games";

	private LinkedHashMap<String, ViewHolderHeader_Game> sHash = new LinkedHashMap<String, ViewHolderHeader_Game>();
	private List<ViewHolderHeader_Game> sessionList = new ArrayList<ViewHolderHeader_Game>();
	private ListAdapter_Game gameAdapter;
	private ExpandableListView elv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		log("onCreate() - about to create activity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_listing);

		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		elv = (ExpandableListView) findViewById(R.id.dbListing);
		gameAdapter = new ListAdapter_Game(View_Games.this, sessionList);

		log("onCreate() - setting adapter");
		elv.setAdapter(gameAdapter);
		log("onCreate() - expanding all");
		expandAll();
		log("onCreate() - setting listeners");
		elv.setOnChildClickListener(elvItemClicked);
		elv.setOnGroupClickListener(elvGroupClicked);
		elv.setOnItemLongClickListener(elvItemLongClicked);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.games).setEnabled(false);
		menu.findItem(R.id.addButton).setVisible(true);
		return true;
	}

	@Override
	public void openAddActivity() {
		Intent intent = new Intent(this, NewGame.class);
		startActivity(intent);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		log("onResume() - refreshing games listing");
		refreshGamesListing();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void expandAll() {
		// method to expand all groups
		int count = gameAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			log("expandAll() - expanding group " + i);
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
		logd("refreshGamesListing() - adding session daos");

		Context context = getApplicationContext();
		Session s;
		Player[] p = new Player[2];

		try {
			Dao<Session, Long> sessionDao = Session.getDao(context);
			Dao<Game, Long> gameDao = Game.getDao(context);
			Dao<Player, Long> playerDao = Player.getDao(context);

			logd("refreshGamesListing() - adding sessions");
			for (Session sess : sessionDao) {
				addSession(sess.getSessionName());
			}

			logd("refreshGamesListing() - adding games");
			for (Game g : gameDao) {
				logd("refreshGamesListing() - got game " + g.getId());
				sessionDao.refresh(g.getSession());
				s = g.getSession();
				p[0] = playerDao.queryForId(g.getFirstPlayer().getId());
				p[1] = playerDao.queryForId(g.getSecondPlayer().getId());

				addGame(s.getSessionName(), String.valueOf(g.getId()),
						p[0].getNickName(), p[1].getNickName(),
						String.valueOf(g.getFirstPlayerScore()) + " / "
								+ String.valueOf(g.getSecondPlayerScore()));
			}
		} catch (SQLException e) {
			loge("Retrieval of games/sessions failed", e);
		}

		log("refreshGamesListing() - done adding games, about to expand all");
		expandAll();
		gameAdapter.notifyDataSetChanged(); // required in case the list has
											// changed
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
					getBaseContext(),
					"Selected " + sessionInfo.getName() + "/"
							+ String.valueOf(gameInfo.getId()),
					Toast.LENGTH_SHORT).show();

			// load the game in progress screen
			Long gid = Long.valueOf(gameInfo.getId());
			Intent intent = new Intent(getApplicationContext(),
					GameInProgress.class);
			intent.putExtra("GID", gid);
			startActivity(intent);
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
						getBaseContext(),
						"Selected " + sessionInfo.getName() + "/"
								+ String.valueOf(gameInfo.getId()),
						Toast.LENGTH_SHORT).show();

				// load the game in progress screen
				Long gid = Long.valueOf(gameInfo.getId());
				Intent intent = new Intent(getApplicationContext(),
						Detail_Game.class);
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

			// get the group header
			ViewHolderHeader_Game sessionInfo = sessionList.get(groupPosition);
			// display it or do something with it
			Toast.makeText(getBaseContext(), "Tapped " + sessionInfo.getName(),
					Toast.LENGTH_SHORT).show();
			return true;
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
