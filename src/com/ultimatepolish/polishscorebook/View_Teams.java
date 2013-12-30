package com.ultimatepolish.polishscorebook;

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
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

import com.ultimatepolish.db.OrmLiteFragment;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.polishscorebook.backend.ListAdapter_Team;
import com.ultimatepolish.polishscorebook.backend.ViewHolderHeader_Team;
import com.ultimatepolish.polishscorebook.backend.ViewHolder_Team;

public class View_Teams extends OrmLiteFragment {
	private static final String LOGTAG = "View_Teams";

	private LinkedHashMap<String, ViewHolderHeader_Team> sHash = new LinkedHashMap<String, ViewHolderHeader_Team>();
	private List<ViewHolderHeader_Team> statusList = new ArrayList<ViewHolderHeader_Team>();
	private ListAdapter_Team teamAdapter;
	private ExpandableListView elv;
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
		teamAdapter = new ListAdapter_Team(context, statusList);
		elv.setAdapter(teamAdapter);
		expandAll();
		elv.setOnChildClickListener(elvItemClicked);
		elv.setOnGroupClickListener(elvGroupClicked);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = getActivity();
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem fav = menu.add("New Team");
		fav.setIcon(R.drawable.ic_menu_add);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		fav.setIntent(new Intent(context, NewTeam.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshTeamsListing();
	}

	private void expandAll() {
		// method to expand all groups
		int count = teamAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.expandGroup(i);
		}
	}

	private void collapseAll() {
		// method to collapse all groups
		int count = teamAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.collapseGroup(i);
		}
	}

	protected void refreshTeamsListing() {
		sHash.clear();
		statusList.clear();

		// add all the statii to the headers
		addStatus("Active");
		addStatus("Retired");

		Player[] p = new Player[2];

		// add all the teams
		// try{
		// Dao<Team, Long> teamDao = Team.getDao(context);
		// Dao<Player, Long> playerDao = Player.getDao(context);
		//
		// for (Team t: teamDao) {
		// playerDao.refresh(t.getFirstPlayer());
		// playerDao.refresh(t.getSecondPlayer());
		//
		// addTeam(t.getIsActive(),
		// String.valueOf(t.getId()),
		// t.getTeamName(),
		// "(" + t.getFirstPlayer().getNickName()
		// + " and " + t.getSecondPlayer().getNickName() + ")"
		// );
		// }
		// }
		// catch (SQLException e){
		// loge("Retrieval of teams failed", e);
		// }

		expandAll();
		teamAdapter.notifyDataSetChanged(); // required in case the list has
											// changed
	}

	private OnChildClickListener elvItemClicked = new OnChildClickListener() {
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			// get the group header
			ViewHolderHeader_Team statusInfo = statusList.get(groupPosition);
			// get the child info
			ViewHolder_Team teamInfo = statusInfo.getTeamList().get(
					childPosition);
			// display it or do something with it
			Toast.makeText(context, "Selected " + teamInfo.getTeamName(),
					Toast.LENGTH_SHORT).show();

			// load the game in progress screen
			Long tId = Long.valueOf(teamInfo.getId());
			Intent intent = new Intent(context, Detail_Team.class);
			intent.putExtra("TID", tId);
			startActivity(intent);
			return false;
		}
	};
	private OnGroupClickListener elvGroupClicked = new OnGroupClickListener() {
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {

			// ViewHolderHeader_Team statusInfo = statusList.get(groupPosition);
			// Toast.makeText(context, "Tapped " + statusInfo.getName(),
			// Toast.LENGTH_SHORT).show();
			return false;
		}
	};

	private void addStatus(String statusName) {
		ViewHolderHeader_Team vhh_Team = new ViewHolderHeader_Team();
		vhh_Team.setName(statusName);
		statusList.add(vhh_Team);
		sHash.put(statusName, vhh_Team);
	}

	private void addTeam(boolean isActive, String teamId, String teamName,
			String teamPlayers) {
		// find the index of the session header
		String sortBy;
		if (isActive) {
			sortBy = "Active";
		} else {
			sortBy = "Retired";
		}
		ViewHolderHeader_Team statusInfo = sHash.get(sortBy);
		try {
			List<ViewHolder_Team> teamList = statusInfo.getTeamList();

			// create a new child and add that to the group
			ViewHolder_Team teamInfo = new ViewHolder_Team();
			teamInfo.setId(teamId);
			teamInfo.setTeamName(teamName);
			teamInfo.setPlayerNames(teamPlayers);
			teamList.add(teamInfo);
			statusInfo.setTeamList(teamList);
		} catch (NullPointerException e) {
			loge("The header " + sortBy + " does not exist", e);
		}
	}

}
