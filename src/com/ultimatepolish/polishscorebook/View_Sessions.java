package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.SessionType;

public class View_Sessions extends MenuContainerActivity {
	private LinkedHashMap<String, ViewHolderHeader_Session> sHash = new LinkedHashMap<String, ViewHolderHeader_Session>();
	private ArrayList<ViewHolderHeader_Session> statusList = new ArrayList<ViewHolderHeader_Session>();
	private ListAdapter_Session sessionAdapter;
	private ExpandableListView elv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_listing);
		
		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        elv = (ExpandableListView) findViewById(R.id.dbListing);
        sessionAdapter = new ListAdapter_Session(View_Sessions.this, statusList);
        elv.setAdapter(sessionAdapter);
        expandAll();
        elv.setOnChildClickListener(elvItemClicked);
        elv.setOnGroupClickListener(elvGroupClicked);
        
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.sessions).setEnabled(false);
		menu.findItem(R.id.addButton).setVisible(true);
		return true;
	}	
	@Override
	public void openAddActivity() {
    	Intent intent = new Intent(this, NewSession.class);
    	startActivity(intent);
    }
	@Override
    protected void onRestart(){
    	super.onRestart();
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshSessionListing();
    }
    @Override
    protected void onStop() {
    	super.onStop();
    }
    private void expandAll() {
    	//method to expand all groups
    	int count = sessionAdapter.getGroupCount();
    	for (int i = 0; i < count; i++){
		elv.expandGroup(i);
    	}
    }
    private void collapseAll() {
    	//method to collapse all groups
    	int count = sessionAdapter.getGroupCount();
    	for (int i = 0; i < count; i++){
    	elv.collapseGroup(i);
    	}
    }
    protected void refreshSessionListing(){
    	sHash.clear();
    	statusList.clear();
    	
    	// add all the statii to the headers
        addStatus("Active");
        addStatus("Inactive");
        
        // add all the sessions
    	Dao<Session, Long> sessionDao = null;
        try{
        	sessionDao = getHelper().getSessionDao();
        	for (Session s: sessionDao) {
        		String isTeam = "Singles";
        		if (s.getIsTeam()) {
        			isTeam = "Doubles";
        		}

        		addSession(s.getIsActive(), 
        				String.valueOf(s.getId()), 
        				s.getSessionName(),
        				SessionType.typeString[s.getSessionType()],
        				isTeam
        				);
        	}
    	}
        catch (SQLException e){
    		Context context = getApplicationContext();
    		Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
    		Log.e(View_Sessions.class.getName(), "Retrieval of sessions failed", e);
        }
        
    	expandAll();
    	sessionAdapter.notifyDataSetChanged(); // required in case the list has changed
    }
    private OnChildClickListener elvItemClicked =  new OnChildClickListener() {
    	public boolean onChildClick(ExpandableListView parent, View v,
    	int groupPosition, int childPosition, long id) {

	    //get the group header
	    ViewHolderHeader_Session statusInfo = statusList.get(groupPosition);
	    //get the child info
   		ViewHolder_Session sessionInfo =  statusInfo.getSessionList().get(childPosition);
   		//display it or do something with it
   		Toast.makeText(getBaseContext(), "Selected " + sessionInfo.getName(), Toast.LENGTH_SHORT).show();
    	
   		// load the game in progress screen
   		Long sId  = Long.valueOf(sessionInfo.getId());
		Intent intent = new Intent(getApplicationContext(), Detail_Session.class);
        intent.putExtra("SID", sId);
        startActivity(intent);
    	return false;
    	}
    };
    private OnGroupClickListener elvGroupClicked =  new OnGroupClickListener() {
    	public boolean onGroupClick(ExpandableListView parent, View v,
    	int groupPosition, long id) {
    	    
    	//get the group header
    	ViewHolderHeader_Session statusInfo = statusList.get(groupPosition);
    	//display it or do something with it
    	Toast.makeText(getBaseContext(), "Tapped " + statusInfo.getName(), Toast.LENGTH_SHORT).show();
    	return false;
    	}
    };
    private void addStatus(String statusName){
    	ViewHolderHeader_Session vhh_Session = new ViewHolderHeader_Session();
    	vhh_Session.setName(statusName);
    	statusList.add(vhh_Session);
    	sHash.put(statusName, vhh_Session);
    }
    private void addSession(boolean isActive, String sessionId, String sessionName, String sessionType, String sessionTeam){
    	//find the index of the session header
    	String sortBy;
    	if (isActive) {
    		sortBy = "Active";
    	} else {
    		sortBy = "Inactive";
    	}
    	ViewHolderHeader_Session statusInfo = sHash.get(sortBy);
	    ArrayList<ViewHolder_Session> sessionList = statusInfo.getSessionList();
	    
	    //create a new child and add that to the group
	    ViewHolder_Session sessionInfo = new ViewHolder_Session();
	    sessionInfo.setId(sessionId);
	    sessionInfo.setName(sessionName);
	    sessionInfo.setType(sessionType);
	    sessionInfo.setTeam(sessionTeam);
	    sessionList.add(sessionInfo);
		statusInfo.setSessionList(sessionList);
	}
}
