package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
import com.ultimatepolish.db.Venue;
import com.ultimatepolish.polishscorebook.backend.ListAdapter_Venue;
import com.ultimatepolish.polishscorebook.backend.ViewHolderHeader_Venue;
import com.ultimatepolish.polishscorebook.backend.ViewHolder_Venue;

public class View_Venues extends OrmLiteFragment {
	private static final String LOGTAG = "View_Venues";

	private LinkedHashMap<String, ViewHolderHeader_Venue> sHash = new LinkedHashMap<String, ViewHolderHeader_Venue>();
	private List<ViewHolderHeader_Venue> statusList = new ArrayList<ViewHolderHeader_Venue>();
	private ListAdapter_Venue venueAdapter;
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
		venueAdapter = new ListAdapter_Venue(context, statusList);
		elv.setAdapter(venueAdapter);
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
		MenuItem fav = menu.add("New Venue");
		fav.setIcon(R.drawable.ic_menu_add);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		fav.setIntent(new Intent(context, NewVenue.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshVenuesListing();
	}

	private void expandAll() {
		// method to expand all groups
		int count = venueAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.expandGroup(i);
		}
	}

	private void collapseAll() {
		// method to collapse all groups
		int count = venueAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			elv.collapseGroup(i);
		}
	}

	protected void refreshVenuesListing() {
		sHash.clear();
		statusList.clear();

		// add all the statii to the headers
		addStatus("Active");
		addStatus("Inactive");

		// add all the venues
		Dao<Venue, Long> venueDao = null;
		try {
			venueDao = getHelper().getVenueDao();
			for (Venue v : venueDao) {
				addVenue(v.getIsActive(), String.valueOf(v.getId()),
						v.getName());
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(View_Games.class.getName(), "Retrieval of venues failed", e);
		}

		expandAll();
		venueAdapter.notifyDataSetChanged(); // required in case the list has
												// changed
	}

	private OnChildClickListener elvItemClicked = new OnChildClickListener() {
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			// get the group header
			ViewHolderHeader_Venue statusInfo = statusList.get(groupPosition);
			// get the child info
			ViewHolder_Venue venueInfo = statusInfo.getVenueList().get(
					childPosition);
			// display it or do something with it
			Toast.makeText(context, "Selected " + venueInfo.getName(),
					Toast.LENGTH_SHORT).show();

			// load the game in progress screen
			Long vId = Long.valueOf(venueInfo.getId());
			Intent intent = new Intent(context, Detail_Venue.class);
			intent.putExtra("VID", vId);
			startActivity(intent);
			return false;
		}
	};
	private OnGroupClickListener elvGroupClicked = new OnGroupClickListener() {
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {

			// ViewHolderHeader_Venue statusInfo =
			// statusList.get(groupPosition);
			// Toast.makeText(context, "Tapped " + statusInfo.getName(),
			// Toast.LENGTH_SHORT).show();
			return false;
		}
	};

	private void addStatus(String statusName) {
		ViewHolderHeader_Venue vhh_Venue = new ViewHolderHeader_Venue();
		vhh_Venue.setName(statusName);
		statusList.add(vhh_Venue);
		sHash.put(statusName, vhh_Venue);
	}

	private void addVenue(Boolean isActive, String venueId, String venueName) {
		// find the index of the session header
		String sortBy;
		if (isActive) {
			sortBy = "Active";
		} else {
			sortBy = "Inactive";
		}
		ViewHolderHeader_Venue statusInfo = sHash.get(sortBy);
		try {
			List<ViewHolder_Venue> venueList = statusInfo.getVenueList();

			// create a new child and add that to the group
			ViewHolder_Venue venueInfo = new ViewHolder_Venue();
			venueInfo.setId(venueId);
			venueInfo.setName(venueName);
			venueList.add(venueInfo);
			statusInfo.setVenueList(venueList);
		} catch (NullPointerException e) {
			loge("The header " + sortBy + " does not exist", e);
		}
	}

}
