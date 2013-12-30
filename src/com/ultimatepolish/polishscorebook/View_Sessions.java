package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.OrmLiteFragment;
import com.ultimatepolish.db.Session;
import com.ultimatepolish.enums.SessionType;
import com.ultimatepolish.polishscorebook.backend.ListAdapter_Session;
import com.ultimatepolish.polishscorebook.backend.ViewHolderHeader_Session;
import com.ultimatepolish.polishscorebook.backend.ViewHolder_Session;

public class View_Sessions extends OrmLiteFragment {
	private static final String LOGTAG = "View_Sessions";

	private LinkedHashMap<String, ViewHolderHeader_Session> sHash = new LinkedHashMap<String, ViewHolderHeader_Session>();
	private ArrayList<ViewHolderHeader_Session> statusList = new ArrayList<ViewHolderHeader_Session>();
	private ListAdapter_Session sessionAdapter;
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
		sessionAdapter = new ListAdapter_Session(context, statusList);
		elv.setAdapter(sessionAdapter);
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
		MenuItem fav = menu.add("New Session");
		fav.setIcon(R.drawable.ic_menu_add);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		fav.setIntent(new Intent(context, NewSession.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshSessionListing();
	}

	private void expandAll() {
		// method to expand all groups
		int count = sessionAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.expandGroup(i);
		}
	}

	private void collapseAll() {
		// method to collapse all groups
		int count = sessionAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.collapseGroup(i);
		}
	}

	protected void refreshSessionListing() {
		sHash.clear();
		statusList.clear();

		// add all the statii to the headers
		addStatus("Active");
		addStatus("Inactive");

		// add all the sessions
		Dao<Session, Long> sessionDao = null;
		try {
			sessionDao = getHelper().getSessionDao();
			for (Session s : sessionDao) {
				String isTeam = "Singles";
				if (s.getIsTeam()) {
					isTeam = "Doubles";
				}

				addSession(s.getIsActive(), String.valueOf(s.getId()),
						s.getSessionName(),
						SessionType.typeString[s.getSessionType()], isTeam);
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(View_Sessions.class.getName(),
					"Retrieval of sessions failed", e);
		}

		expandAll();
		sessionAdapter.notifyDataSetChanged(); // required in case the list has
												// changed
	}

	private OnChildClickListener elvItemClicked = new OnChildClickListener() {
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			// get the group header
			ViewHolderHeader_Session statusInfo = statusList.get(groupPosition);
			// get the child info
			ViewHolder_Session sessionInfo = statusInfo.getSessionList().get(
					childPosition);
			// display it or do something with it
			Toast.makeText(context, "Selected " + sessionInfo.getName(),
					Toast.LENGTH_SHORT).show();

			// load the game in progress screen
			Long sId = Long.valueOf(sessionInfo.getId());
			Intent intent = new Intent(context, Detail_Session.class);
			intent.putExtra("SID", sId);
			startActivity(intent);
			return false;
		}
	};
	private OnGroupClickListener elvGroupClicked = new OnGroupClickListener() {
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {

			// ViewHolderHeader_Session statusInfo =
			// statusList.get(groupPosition);
			// Toast.makeText(context, "Tapped " + statusInfo.getName(),
			// Toast.LENGTH_SHORT).show();
			return false;
		}
	};

	private void addStatus(String statusName) {
		ViewHolderHeader_Session vhh_Session = new ViewHolderHeader_Session();
		vhh_Session.setName(statusName);
		statusList.add(vhh_Session);
		sHash.put(statusName, vhh_Session);
	}

	private void addSession(boolean isActive, String sessionId,
			String sessionName, String sessionType, String sessionTeam) {
		// find the index of the session header
		String sortBy;
		if (isActive) {
			sortBy = "Active";
		} else {
			sortBy = "Inactive";
		}
		ViewHolderHeader_Session statusInfo = sHash.get(sortBy);
		ArrayList<ViewHolder_Session> sessionList = statusInfo.getSessionList();

		// create a new child and add that to the group
		ViewHolder_Session sessionInfo = new ViewHolder_Session();
		sessionInfo.setId(sessionId);
		sessionInfo.setName(sessionName);
		sessionInfo.setType(sessionType);
		sessionInfo.setTeam(sessionTeam);
		sessionList.add(sessionInfo);
		statusInfo.setSessionList(sessionList);
	}
}
